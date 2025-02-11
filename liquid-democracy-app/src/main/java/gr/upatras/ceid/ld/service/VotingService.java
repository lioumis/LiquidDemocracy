package gr.upatras.ceid.ld.service;

import gr.upatras.ceid.ld.dto.*;
import gr.upatras.ceid.ld.entity.*;
import gr.upatras.ceid.ld.enums.Action;
import gr.upatras.ceid.ld.enums.Role;
import gr.upatras.ceid.ld.enums.VotingType;
import gr.upatras.ceid.ld.exception.AuthorizationException;
import gr.upatras.ceid.ld.exception.ValidationException;
import gr.upatras.ceid.ld.exception.VotingCreationException;
import gr.upatras.ceid.ld.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class VotingService {

    public static final String SIMPLE_DATE_FORMAT = "yyyy-MM-dd"; //TODO: Transfer to a DateHelper

    private final UserRepository userRepository;

    private final VotingRepository votingRepository;

    private final VoteRepository voteRepository;

    private final DelegationRepository delegationRepository;

    private final AuditLogRepository auditLogRepository;

    private final TopicRepository topicRepository;

    private final MessageRepository messageRepository;

    private final MessageDetailsRepository messageDetailsRepository;

    private final FeedbackRepository feedbackRepository;

    private final ParticipantRepository participantRepository;

    public VotingService(UserRepository userRepository, VotingRepository votingRepository,
                         VoteRepository voteRepository, DelegationRepository delegationRepository,
                         AuditLogRepository auditLogRepository, TopicRepository topicRepository,
                         MessageRepository messageRepository, MessageDetailsRepository messageDetailsRepository,
                         FeedbackRepository feedbackRepository, ParticipantRepository participantRepository) {
        this.userRepository = userRepository;
        this.votingRepository = votingRepository;
        this.voteRepository = voteRepository;
        this.delegationRepository = delegationRepository;
        this.auditLogRepository = auditLogRepository;
        this.topicRepository = topicRepository;
        this.messageRepository = messageRepository;
        this.messageDetailsRepository = messageDetailsRepository;
        this.feedbackRepository = feedbackRepository;
        this.participantRepository = participantRepository;
    }

    @Transactional
    public void castVote(String username, Long votingId, List<String> voteChoices) throws ValidationException {
        UserEntity voter = userRepository.findByUsername(username)
                .orElseThrow(() -> new ValidationException("Ο ψηφοφόρος δεν βρέθηκε"));

        VotingEntity voting = votingRepository.findById(votingId)
                .orElseThrow(() -> new ValidationException("Η ψηφοφορία δεν βρέθηκε"));

        if (voting.getEndDate().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Η ψηφοφορία έχει λήξει.");
        }

        if (!voting.getElectoralCommittee().contains(voter)) {
            ParticipantEntity participantEntity = participantRepository.findByUserAndVoting(voter, voting)
                    .orElseThrow(() -> new ValidationException("Δεν υπάρχει αίτηση συμμετοχής στην ψηφοφορία"));

            if (participantEntity.getStatus() == null) {
                throw new ValidationException("Η συμμετοχή σας σε αυτή την ψηφοφορία δεν έχει εξεταστεί ακόμα");
            }

            if (Boolean.FALSE.equals(participantEntity.getStatus())) {
                throw new ValidationException("Η συμμετοχή σας σε αυτή την ψηφοφορία έχει απορριφθεί");
            }
        }

        if (voteChoices == null || voteChoices.isEmpty()) {
            throw new ValidationException("Δεν υπάρχει επιλογή");
        }

        if (voting.getVotingType().equals(VotingType.SINGLE) && voteChoices.size() > 1) {
            throw new ValidationException("Η ψηφοφορία δεν επιτρέπει πολλές επιλογές");
        }

        if (voting.getVotingType().equals(VotingType.MULTIPLE) && voting.getVoteLimit() != null && voteChoices.size() > voting.getVoteLimit()) {
            throw new ValidationException("Έχετε κάνει παραπάνω επιλογές από τις επιτρεπόμενες");
        }

        if (voteRepository.existsByOriginalVoterAndVoting(voter, voting)) {
            throw new ValidationException("Έχετε ήδη ψηφίσει για αυτή την ψηφοφορία.");
        }

        Optional<DelegationEntity> delegationOpt = delegationRepository.findByDelegatorAndVoting(voter, voting);
        if (delegationOpt.isPresent()) {
            throw new ValidationException("Έχετε ήδη αναθέσει την ψήφο σας σε άλλο χρήστη και δεν μπορείτε να ψηφίσετε άμεσα.");
        }

        List<VotingOptionsEntity> votingOptions = voting.getVotingOptions();
        Set<VotingOptionsEntity> selectedOptions = new HashSet<>();

        VoteEntity vote = new VoteEntity(voter, voting);

        for (String voteChoice : voteChoices) {
            VotingOptionsEntity votingOption = votingOptions.stream().filter(option -> voteChoice.equals(option.getName())).findFirst()
                    .orElseThrow(() -> new ValidationException("Η επιλογή δεν βρέθηκε"));
            selectedOptions.add(votingOption);
        }

        selectedOptions.forEach(votingOption -> {
            VoteDetailsEntity voteDetailsEntity = new VoteDetailsEntity(1, votingOption);
            vote.addVoteDetails(voteDetailsEntity);
        });

        voteRepository.save(vote);

        AuditLogEntity auditLog = new AuditLogEntity(voter, Action.DIRECT_VOTE, "Ο χρήστης " + voter.getUsername() + " ψήφισε για την ψηφοφορία " + votingId + ".");
        auditLogRepository.save(auditLog);

        castDelegatedVote(voter, voting, selectedOptions, voter);
    }

    public void castDelegatedVote(UserEntity delegate, VotingEntity voting, Set<VotingOptionsEntity> votingOptions, UserEntity finalDelegate) {
        List<DelegationEntity> delegations = delegationRepository.findByDelegateAndVoting(delegate, voting);

        if (delegations.isEmpty()) {
            return;
        }

        for (DelegationEntity delegation : delegations) {
            if (!voteRepository.existsByOriginalVoterAndVoting(delegation.getDelegator(), voting)) {
                VoteEntity vote = new VoteEntity(finalDelegate, delegation.getDelegator(), voting);

                votingOptions.forEach(votingOption -> {
                    VoteDetailsEntity voteDetailsEntity = new VoteDetailsEntity(1, votingOption);
                    vote.addVoteDetails(voteDetailsEntity);
                });

                voteRepository.save(vote);

                AuditLogEntity auditLog = new AuditLogEntity(finalDelegate, Action.DELEGATED_VOTE,
                        "Ο χρήστης " + delegate.getUsername() + " ψήφισε για την ψηφοφορία " + voting.getId() + " εκ μέρους του " + delegation.getDelegator().getUsername() + ".");
                auditLogRepository.save(auditLog);
            }

            castDelegatedVote(delegation.getDelegator(), voting, votingOptions, finalDelegate);
        }
    }

    @Transactional
    public void initializeVoting(String username, VotingInitializationDto votingInitializationDto) throws ValidationException, VotingCreationException {
        if (votingInitializationDto == null) {
            throw new ValidationException("Εσφαλμένα δεδομένα");
        }

        if (votingInitializationDto.name() == null || votingInitializationDto.name().trim().isEmpty()) {
            throw new ValidationException("Ο τίτλος της ψηφοφορίας είναι κενός");
        }

        if (votingInitializationDto.committee() == null || votingInitializationDto.committee().isEmpty()) {
            throw new ValidationException("Η εφορευτική επιτροπή είναι κενή");
        }

        if (votingInitializationDto.committee().size() != 3) {
            throw new ValidationException("Η εφορευτική επιτροπή πρέπει να αποτελείται από 3 άτομα");
        }

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ValidationException("Ο χρήστης δεν βρέθηκε"));

        TopicEntity topic = topicRepository.findByTitle(votingInitializationDto.topic())
                .orElseThrow(() -> new ValidationException("Η θεματική περιοχή δεν βρέθηκε"));

        if (votingRepository.existsByNameIgnoreCase(votingInitializationDto.name())) {
            throw new ValidationException("Υπάρχει ήδη ψηφοφορία με αυτό το όνομα");
        }

        Map<Integer, String> errorMetaData = new HashMap<>();
        Set<UserEntity> committee = new HashSet<>();

        for (int i = 0; i < votingInitializationDto.committee().size(); i++) {
            String usernameString = votingInitializationDto.committee().get(i);
            Optional<UserEntity> memberOptional = userRepository.findByUsername(usernameString);
            if (memberOptional.isEmpty()) {
                errorMetaData.put(i, "Το μέλος δεν βρέθηκε");//TODO: Handle committee roles
            } else {
                if (!committee.add(memberOptional.get())) {
                    throw new ValidationException("Παρακαλώ εισάγετε διαφορετικούς χρήστες");
                }
            }
        }

        if (!errorMetaData.isEmpty()) {
            throw new VotingCreationException("Τουλάχιστον ένα από τα μέλη δεν βρέθηκε", errorMetaData);
        }

        VotingEntity votingEntity = new VotingEntity(votingInitializationDto.name(), topic, committee);

        votingRepository.save(votingEntity);

        AuditLogEntity auditLog = new AuditLogEntity(user, Action.VOTING_CREATION,
                "Ο χρήστης " + user.getUsername() + " δημιούργησε την ψηφοφορία " + votingEntity.getId() +
                        " με τίτλο " + votingInitializationDto.name() + " στη θεματική περιοχή " + topic.getTitle() + ".");
        auditLogRepository.save(auditLog);
    }

    @Transactional
    public void editVoting(String username, VotingCreationDto votingCreationDto) throws ValidationException, AuthorizationException {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ValidationException("Ο χρήστης δεν βρέθηκε"));

        VotingEntity voting = votingRepository.findById(votingCreationDto.id())
                .orElseThrow(() -> new ValidationException("Η ψηφοφορία δεν βρέθηκε"));

        if (!voting.getElectoralCommittee().contains(user)) {
            throw new AuthorizationException("Δεν ανήκετε στην εφορευτική επιτροπή της ψηφοφορίας");
        }

        if (votingCreationDto.startDate() != null) {
            //TODO: Validate format & business
            voting.setStartDate(toLocalDateTime(votingCreationDto.startDate()));
        }

        if (votingCreationDto.endDate() != null) {
            //TODO: Validate format & business
            voting.setEndDate(toLocalDateTime(votingCreationDto.endDate()));
        }

        if (votingCreationDto.description() != null && !votingCreationDto.description().trim().isEmpty()) {
            //TODO: Validate length
            voting.setInformation(votingCreationDto.description());
        }

        if (votingCreationDto.mechanism() != null) {
            try {
                VotingType type = VotingType.valueOf(votingCreationDto.mechanism());
                voting.setVotingType(type);
            } catch (IllegalArgumentException e) {
                throw new ValidationException("Ο τύπος ψηφοφορίας είναι εσφαλμένος");
            }
        }

        if (votingCreationDto.options() != null && !votingCreationDto.options().isEmpty()) {
            //TODO: Validate each and if all valid, proceed to replace all
            voting.clearVotingOptions();
            votingCreationDto.options().forEach(option ->
                    voting.addVotingOption(option.title(), option.details()));
        }

        if (votingCreationDto.comment() != null && !votingCreationDto.comment().trim().isEmpty()) {
            //TODO: Validate length
            //TODO: Clear the old one or just add?
            voting.addMessage(votingCreationDto.comment(), user);
        }

        if (votingCreationDto.voteLimit() != null) {
            //TODO: Validate if smaller than the number of options & if the voting is of multiple choice type
            voting.setVoteLimit(votingCreationDto.voteLimit().intValue());
        }

        votingRepository.save(voting);

        AuditLogEntity auditLog = new AuditLogEntity(user, Action.VOTING_CREATION,
                "Ο χρήστης " + user.getUsername() + " επεξεργάστηκε την ψηφοφορία " + voting.getId() +
                        " με τίτλο " + voting.getName() + " στη θεματική περιοχή " + voting.getTopic().getTitle() + ".");
        auditLogRepository.save(auditLog);
    }

    public void requestAccess(String username, Long votingId) throws ValidationException {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ValidationException("Ο ψηφοφόρος δεν βρέθηκε"));

        VotingEntity voting = votingRepository.findById(votingId)
                .orElseThrow(() -> new ValidationException("Η ψηφοφορία δεν βρέθηκε"));

        if (participantRepository.existsByUserAndVoting(user, voting)) {
            throw new ValidationException("Υπάρχει ήδη αίτημα συμμετοχής για τη συγκεκριμένη ψηφοφορία");
        }

        if (voting.getStartDate() == null) {
            throw new ValidationException("Η καταχώρηση αιτήματος συμμετοχής δεν είναι δυνατή ακόμα");
        }

        if (voting.getStartDate().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Η καταχώρηση αιτήματος συμμετοχής δεν είναι πλέον δυνατή");
        }

        if (voting.getEndDate().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Η ψηφοφορία έχει λήξει");
        }

        ParticipantEntity participantEntity = new ParticipantEntity(user, voting);
        participantRepository.save(participantEntity);
    }

    public List<ParticipationRequestDto> getRequests(Long votingId) throws ValidationException {
        VotingEntity voting = votingRepository.findById(votingId)
                .orElseThrow(() -> new ValidationException("Η ψηφοφορία δεν βρέθηκε"));

        if (voting.getStartDate() == null) {
            throw new ValidationException("Η ψηφοφορία δεν έχει ημερομηνία έναρξης");
        }

        if (voting.getStartDate().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Η ψηφοφορία έχει ήδη ξεκινήσει");
        }

        return participantRepository.findByVotingAndStatusIs(voting, null).stream().map(p ->
                new ParticipationRequestDto(p.getId().intValue(), p.getUser().getName(), p.getUser().getSurname(),
                        p.getUser().getUsername())).toList();
    }

    public void processRequest(String username, Long requestId, boolean approve) throws ValidationException, AuthorizationException {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ValidationException("Ο χρήστης δεν βρέθηκε"));

        ParticipantEntity participant = participantRepository.findById(requestId)
                .orElseThrow(() -> new ValidationException("Το αίτημα δεν βρέθηκε"));

        if (participant.getStatus() != null) {
            throw new ValidationException("Το αίτημα είναι ήδη επεξεργασμένο");
        }

        VotingEntity voting = participant.getVoting();

        if (!voting.getElectoralCommittee().contains(user)) {
            throw new AuthorizationException("Δεν ανήκετε στην εφορευτική επιτροπή αυτής της ψηφοφορίας");
        }

        participant.setStatus(approve);
        participantRepository.save(participant);
    }

    public VotingAccessDto hasAccess(String username, Long votingId) throws ValidationException {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ValidationException("Ο ψηφοφόρος δεν βρέθηκε"));

        VotingEntity voting = votingRepository.findById(votingId)
                .orElseThrow(() -> new ValidationException("Η ψηφοφορία δεν βρέθηκε"));

        if (voting.getElectoralCommittee().contains(user)) {
            return new VotingAccessDto(true, true);
        }

        if (voting.getEndDate() != null && voting.getEndDate().isBefore(LocalDateTime.now())) {
            return new VotingAccessDto(true, true);
        }

        Optional<ParticipantEntity> participationEntityOptional = participantRepository.findByUserAndVoting(user, voting);

        if (participationEntityOptional.isEmpty()) {
            return new VotingAccessDto(false, null);
        }

        ParticipantEntity participation = participationEntityOptional.get();

        return new VotingAccessDto(true, participation.getStatus());
    }

    public List<VotingDto> getVotings(String username, Role selectedRole) throws ValidationException {
        UserEntity voter = userRepository.findByUsername(username)
                .orElseThrow(() -> new ValidationException("Ο ψηφοφόρος δεν βρέθηκε"));

        List<VotingEntity> votingEntities = votingRepository.findAll();

        if (Role.ELECTORAL_COMMITTEE.equals(selectedRole)) {
            return votingEntities.stream().filter(v -> v.getElectoralCommittee().contains(voter)).map(v -> {
                boolean hasVoted = v.getVotes().stream().anyMatch(vote -> vote.getOriginalVoter().getId().equals(voter.getId()));
                return new VotingDto(v.getName(), v.getTopic().getTitle(),
                        toString(v.getStartDate()), toString(v.getEndDate()), hasVoted, v.getVotes().size(),
                        v.getId().intValue());
            }).toList();
        }

        return votingEntities.stream().filter(v -> v.getStartDate() != null).map(v -> {
            boolean hasVoted = v.getVotes().stream().anyMatch(vote -> vote.getOriginalVoter().getId().equals(voter.getId()));
            return new VotingDto(v.getName(), v.getTopic().getTitle(),
                    toString(v.getStartDate()), toString(v.getEndDate()), hasVoted, v.getVotes().size(),
                    v.getId().intValue());
        }).toList();
    }

    public List<SuggestedVotingDto> getSuggestedVotings() {
        Pageable pageable = PageRequest.of(0, 6);
        List<VotingEntity> topVotings = votingRepository.findTopVotingsWithMostVotesAndComments(pageable);
        return topVotings.stream().map(v -> new SuggestedVotingDto(v.getName(), v.getTopic().getTitle(),
                v.getVotes().size(), v.getMessages().size(), v.getId().intValue())).toList();
    }

    public VotingDetailsDto getVotingDetails(String username, Long votingId) throws ValidationException, AuthorizationException {
        VotingEntity voting = votingRepository.findById(votingId)
                .orElseThrow(() -> new ValidationException("Η ψηφοφορία δεν βρέθηκε"));

        UserEntity voter = userRepository.findByUsername(username)
                .orElseThrow(() -> new ValidationException("Ο ψηφοφόρος δεν βρέθηκε"));


        if (voting.getStartDate() != null && voting.getEndDate() != null && voting.getEndDate().isBefore(LocalDateTime.now())) {
            return getInactiveVotingStatistics(voting, voter);
        }

        if (voting.getStartDate() == null || voting.getEndDate() == null) {
            if (voting.getElectoralCommittee().contains(voter)) {
                return getVotingPreviewDetails(voting);
            }
            throw new AuthorizationException("Δεν ανήκετε στην εφορευτική επιτροπή αυτής της ψηφοφορίας");
        }

        return getActiveVotingDetails(voting, voter);
    }

    public List<VotingTitleDto> getVotingTitles() {
        return votingRepository.findAll().stream().map(votingEntity ->
                new VotingTitleDto(votingEntity.getId().intValue(), votingEntity.getName())).toList();
    }

    public List<DiscussionDto> getDiscussions(String username, Long votingId) throws ValidationException {
        VotingEntity voting = votingRepository.findById(votingId)
                .orElseThrow(() -> new ValidationException("Η ψηφοφορία δεν βρέθηκε"));

        UserEntity voter = userRepository.findByUsername(username)
                .orElseThrow(() -> new ValidationException("Ο χρήστης δεν βρέθηκε"));

        List<MessageEntity> messages = messageRepository.findByVoting(voting);
        return messages.stream().map(message -> {
            List<MessageDetailsEntity> messageDetails = message.getMessageDetails();

            long likes = message.getMessageDetails().stream().filter(MessageDetailsEntity::isLiked).count();
            long dislikes = message.getMessageDetails().size() - likes;

            Optional<MessageDetailsEntity> userDetails = messageDetails.stream().filter(details ->
                    details.getUser().getId().equals(voter.getId())).findFirst();

            Boolean userAction = null;
            if (userDetails.isPresent()) {
                MessageDetailsEntity messageDetailsEntity = userDetails.get();
                userAction = messageDetailsEntity.isLiked();
            }

            return new DiscussionDto(message.getId().intValue(), message.getUser().getName(), message.getUser().getSurname(),
                    message.getContent(), (int) likes, (int) dislikes, userAction);
        }).toList();
    }

    public void reactToMessage(Long messageId, String username, boolean action) throws ValidationException {
        MessageEntity message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ValidationException("Το μήνυμα δεν βρέθηκε"));

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ValidationException("Ο χρήστης δεν βρέθηκε"));


        if (message.getVoting().getEndDate() != null && message.getVoting().getEndDate().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Η ψηφοφορία έχει λήξει");
        }

        Optional<MessageDetailsEntity> messageDetails = messageDetailsRepository.findByMessageAndUser(message, user);

        if (messageDetails.isPresent()) {
            if ((action && messageDetails.get().isLiked()) || (!action && !messageDetails.get().isLiked())) {
                messageDetailsRepository.delete(messageDetails.get());
            } else {
                MessageDetailsEntity details = messageDetails.get();
                details.setLiked(action);
                messageDetailsRepository.save(details);
            }
        } else {
            MessageDetailsEntity newReaction = new MessageDetailsEntity(message, user, action);
            messageDetailsRepository.save(newReaction);
        }
    }

    public void addComment(String username, Long votingId, String message) throws ValidationException {
        VotingEntity voting = votingRepository.findById(votingId)
                .orElseThrow(() -> new ValidationException("Η ψηφοφορία δεν βρέθηκε"));

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ValidationException("Ο χρήστης δεν βρέθηκε"));

        voting.addMessage(message, user);
        votingRepository.save(voting);
    }

    public void addFeedback(String username, Long votingId, String message) throws ValidationException {
        VotingEntity voting = votingRepository.findById(votingId)
                .orElseThrow(() -> new ValidationException("Η ψηφοφορία δεν βρέθηκε"));

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ValidationException("Ο χρήστης δεν βρέθηκε"));

        if (feedbackRepository.existsByVotingAndUser(voting, user)) {
            throw new ValidationException("Υπάρχει ήδη ανατροφοδότηση");
        }

        FeedbackEntity feedback = new FeedbackEntity(user, message, voting);
        feedbackRepository.save(feedback);
    }

    public List<FeedbackDto> getFeedback(Long votingId) throws ValidationException {
        VotingEntity voting = votingRepository.findById(votingId)
                .orElseThrow(() -> new ValidationException("Η ψηφοφορία δεν βρέθηκε"));

        if (!voting.getEndDate().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Η ψηφοφορία είναι ακόμα ενεργή");
        }

        return feedbackRepository.findByVoting(voting).stream().map(feedback ->
                new FeedbackDto(feedback.getContent())).toList();
    }

    private VotingDetailsDto getInactiveVotingStatistics(VotingEntity voting, UserEntity voter) {
        Map<VotingOptionDto, Integer> resultMap = new HashMap<>();
        AtomicInteger directVotes = new AtomicInteger();
        AtomicInteger delegatedVotes = new AtomicInteger();
        Set<VotingOptionDto> userOptions = new HashSet<>();
        Boolean delegated = null;
        Optional<VoteEntity> voteEntityOptional = voteRepository.findByOriginalVoterAndVoting(voter, voting);

        if (voteEntityOptional.isPresent()) {
            VoteEntity vote = voteEntityOptional.get();
            List<VoteDetailsEntity> voteDetails = vote.getVoteDetails();
            if (voteDetails != null && !voteDetails.isEmpty()) {
                voteDetails.forEach(details -> {
                    VotingOptionsEntity votingOption = details.getVotingOption();
                    userOptions.add(new VotingOptionDto(votingOption.getName(), votingOption.getDescription()));
                });

            }
            delegated = vote.getVoter() != null;
        }

        voting.getVotingOptions().forEach(option -> {
            VotingOptionDto votingOptionDto = new VotingOptionDto(option.getName(), option.getDescription());
            resultMap.put(votingOptionDto, 0);
        });

        List<VoteEntity> votes = voting.getVotes();
        votes.forEach(v -> {
            List<VoteDetailsEntity> voteDetailsEntities = v.getVoteDetails();
            voteDetailsEntities.forEach(details -> {
                VotingOptionDto votingOptionDto = new VotingOptionDto(details.getVotingOption().getName(), details.getVotingOption().getDescription());
                resultMap.merge(votingOptionDto, 1, Integer::sum);
            });

            if (v.getVoter() != null) {
                delegatedVotes.incrementAndGet();
            } else {
                directVotes.incrementAndGet();
            }
        });

        List<VotingResultDto> results = resultMap.entrySet().stream().map(entry ->
                new VotingResultDto(entry.getKey(), entry.getValue())).toList();

        Optional<FeedbackEntity> byVotingAndUser = feedbackRepository.findByVotingAndUser(voting, voter);
        String feedback = byVotingAndUser.map(FeedbackEntity::getContent).orElse(null);

        List<VotingOptionDto> userOptionsList = null;
        if (!userOptions.isEmpty()) {
            userOptionsList = new ArrayList<>(userOptions);
        }

        return new VotingDetailsDto(voting.getName(), voting.getTopic().getTitle(),
                toString(voting.getStartDate()), toString(voting.getEndDate()), voting.getInformation(),
                delegated, voting.getVotingType().getId(), voting.getVoteLimit(), results, userOptionsList, directVotes.get(), delegatedVotes.get(), feedback);
    }

    private VotingDetailsDto getVotingPreviewDetails(VotingEntity voting) {
        List<VotingOptionsEntity> votingOptions = voting.getVotingOptions();
        List<VotingResultDto> votingResults = new ArrayList<>();

        if (votingOptions != null) {
            votingResults = votingOptions.stream().map(option ->
                    new VotingResultDto(new VotingOptionDto(option.getName(), option.getDescription()), null)).toList();
        }

        Integer votingTypeId = voting.getVotingType() == null ? null : voting.getVotingType().getId();

        return new VotingDetailsDto(voting.getName(), voting.getTopic().getTitle(),
                toString(voting.getStartDate()), toString(voting.getEndDate()), voting.getInformation(),
                null, votingTypeId, voting.getVoteLimit(), votingResults, null, null,
                null, null);
    }

    private VotingDetailsDto getActiveVotingDetails(VotingEntity voting, UserEntity voter) {
        Optional<VoteEntity> voteEntityOptional = voteRepository.findByOriginalVoterAndVoting(voter, voting);
        List<VotingOptionsEntity> votingOptions = voting.getVotingOptions();
        List<VotingResultDto> votingResults = votingOptions.stream().map(option ->
                new VotingResultDto(new VotingOptionDto(option.getName(), option.getDescription()), null)).toList();

        if (voteEntityOptional.isPresent()) {
            VoteEntity vote = voteEntityOptional.get();
            List<VoteDetailsEntity> voteDetailsEntities = vote.getVoteDetails();
            List<VotingOptionDto> votingOptionDtos = voteDetailsEntities.stream().map(voteDetailsEntity ->
                    new VotingOptionDto(voteDetailsEntity.getVotingOption().getName(), voteDetailsEntity.getVotingOption().getDescription())).toList();

            return new VotingDetailsDto(voting.getName(), voting.getTopic().getTitle(),
                    toString(voting.getStartDate()), toString(voting.getEndDate()), voting.getInformation(),
                    vote.getVoter() != null, voting.getVotingType().getId(), voting.getVoteLimit(), votingResults, votingOptionDtos, null,
                    null, null);
        }

        return new VotingDetailsDto(voting.getName(), voting.getTopic().getTitle(),
                toString(voting.getStartDate()), toString(voting.getEndDate()), voting.getInformation(),
                null, voting.getVotingType().getId(), voting.getVoteLimit(), votingResults, null, null,
                null, null);
    }

    private String toString(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return "";
        }
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(SIMPLE_DATE_FORMAT);
        return localDateTime.format(formatter);
    }

    private LocalDateTime toLocalDateTime(String string) {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(SIMPLE_DATE_FORMAT);
        LocalDate localDate = LocalDate.parse(string, formatter);
        return localDate.atStartOfDay();
    }
}
