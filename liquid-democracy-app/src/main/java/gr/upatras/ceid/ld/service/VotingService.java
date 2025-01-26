package gr.upatras.ceid.ld.service;

import gr.upatras.ceid.ld.dto.*;
import gr.upatras.ceid.ld.entity.*;
import gr.upatras.ceid.ld.enums.Action;
import gr.upatras.ceid.ld.enums.VotingType;
import gr.upatras.ceid.ld.exception.ValidationException;
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

    public VotingService(UserRepository userRepository, VotingRepository votingRepository,
                         VoteRepository voteRepository, DelegationRepository delegationRepository,
                         AuditLogRepository auditLogRepository, TopicRepository topicRepository,
                         MessageRepository messageRepository, MessageDetailsRepository messageDetailsRepository,
                         FeedbackRepository feedbackRepository) {
        this.userRepository = userRepository;
        this.votingRepository = votingRepository;
        this.voteRepository = voteRepository;
        this.delegationRepository = delegationRepository;
        this.auditLogRepository = auditLogRepository;
        this.topicRepository = topicRepository;
        this.messageRepository = messageRepository;
        this.messageDetailsRepository = messageDetailsRepository;
        this.feedbackRepository = feedbackRepository;
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
    public void createVoting(String username, VotingCreationDto votingCreationDto) throws ValidationException {
        //TODO: Validation

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ValidationException("Voter not found"));

        TopicEntity topic = topicRepository.findById(Long.valueOf(votingCreationDto.topic())) //TODO: Find by title
                .orElseThrow(() -> new ValidationException("Topic not found"));

        VotingEntity votingEntity = new VotingEntity(votingCreationDto.name(), votingCreationDto.description(), //TODO: Check if already exists
                toLocalDateTime(votingCreationDto.startDate()), toLocalDateTime(votingCreationDto.endDate()),
                VotingType.valueOf(votingCreationDto.mechanism()), topic, Set.of(user)); //TODO: What about the committee?

        votingCreationDto.options().forEach(option ->
                votingEntity.addVotingOption(option.title(), option.details()));

        votingEntity.addMessage(votingCreationDto.comment(), user); //TODO: Message is optional

        votingRepository.save(votingEntity);

        AuditLogEntity auditLog = new AuditLogEntity(user, Action.VOTING_CREATION,
                "Ο χρήστης " + user.getUsername() + " δημιούργησε την ψηφοφορία " + votingEntity.getId() +
                        " με τίτλο " + votingCreationDto.name() + " στη θεματική περιοχή " + topic.getTitle() + ".");
        auditLogRepository.save(auditLog);
    }

    public List<VotingDto> getVotings(String username) throws ValidationException {
        UserEntity voter = userRepository.findByUsername(username)
                .orElseThrow(() -> new ValidationException("Voter not found"));

        List<VotingEntity> votingEntities = votingRepository.findAll();
        return votingEntities.stream().map(v -> {
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

    public VotingDetailsDto getVotingDetails(String username, Long votingId) throws ValidationException {
        VotingEntity voting = votingRepository.findById(votingId)
                .orElseThrow(() -> new ValidationException("Voting not found"));

        UserEntity voter = userRepository.findByUsername(username)
                .orElseThrow(() -> new ValidationException("Voter not found"));


        if (voting.getEndDate().isBefore(LocalDateTime.now())) {
            return getInactiveVotingStatistics(voting, voter);
        }

        return getActiveVotingDetails(voting, voter);
    }

    public List<VotingTitleDto> getVotingTitles() {
        return votingRepository.findAll().stream().map(votingEntity ->
                new VotingTitleDto(votingEntity.getId().intValue(), votingEntity.getName())).toList();
    }

    public List<DiscussionDto> getDiscussions(String username, Long votingId) throws ValidationException {
        VotingEntity voting = votingRepository.findById(votingId)
                .orElseThrow(() -> new ValidationException("Voting not found"));

        UserEntity voter = userRepository.findByUsername(username)
                .orElseThrow(() -> new ValidationException("User not found"));

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
                .orElseThrow(() -> new ValidationException("Message not found"));

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ValidationException("User not found"));

        //TODO: Validate if voting is active?
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
                .orElseThrow(() -> new ValidationException("Voting not found"));

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ValidationException("User not found"));

        voting.addMessage(message, user);
        votingRepository.save(voting);
    }

    public void addFeedback(String username, Long votingId, String message) throws ValidationException {
        VotingEntity voting = votingRepository.findById(votingId)
                .orElseThrow(() -> new ValidationException("Voting not found"));

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ValidationException("User not found"));

        if (feedbackRepository.existsByVotingAndUser(voting, user)) {
            throw new ValidationException("Feedback already exists");
        }

        FeedbackEntity feedback = new FeedbackEntity(user, message, voting);
        feedbackRepository.save(feedback);
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
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(SIMPLE_DATE_FORMAT);
        return localDateTime.format(formatter);
    }

    private LocalDateTime toLocalDateTime(String string) {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(SIMPLE_DATE_FORMAT);
        LocalDate localDate = LocalDate.parse(string, formatter);
        return localDate.atStartOfDay();
    }
}
