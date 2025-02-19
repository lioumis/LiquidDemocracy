package gr.upatras.ceid.ld.voting.service;

import gr.upatras.ceid.ld.common.auditlog.service.LoggingService;
import gr.upatras.ceid.ld.common.enums.Action;
import gr.upatras.ceid.ld.common.enums.Role;
import gr.upatras.ceid.ld.common.enums.VotingType;
import gr.upatras.ceid.ld.common.exception.AuthorizationException;
import gr.upatras.ceid.ld.common.exception.ValidationException;
import gr.upatras.ceid.ld.common.exception.VotingCreationException;
import gr.upatras.ceid.ld.common.utils.DateHelper;
import gr.upatras.ceid.ld.delegation.entity.DelegationEntity;
import gr.upatras.ceid.ld.delegation.repository.DelegationRepository;
import gr.upatras.ceid.ld.topic.entity.TopicEntity;
import gr.upatras.ceid.ld.topic.repository.TopicRepository;
import gr.upatras.ceid.ld.user.entity.UserEntity;
import gr.upatras.ceid.ld.user.repository.UserRepository;
import gr.upatras.ceid.ld.voting.dto.*;
import gr.upatras.ceid.ld.voting.entity.*;
import gr.upatras.ceid.ld.voting.repository.*;
import gr.upatras.ceid.ld.voting.validator.VotingValidator;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class VotingService {
    private static final String USER_NOT_FOUND = "Ο χρήστης δεν βρέθηκε";
    private static final String VOTER_NOT_FOUND = "Ο ψηφοφόρος δεν βρέθηκε";
    private static final String VOTING_NOT_FOUND = "Η ψηφοφορία δεν βρέθηκε";

    private final UserRepository userRepository;

    private final VotingRepository votingRepository;

    private final VoteRepository voteRepository;

    private final DelegationRepository delegationRepository;

    private final LoggingService loggingService;

    private final TopicRepository topicRepository;

    private final MessageRepository messageRepository;

    private final MessageDetailsRepository messageDetailsRepository;

    private final FeedbackRepository feedbackRepository;

    private final ParticipantRepository participantRepository;

    private final VotingValidator votingValidator;

    public VotingService(UserRepository userRepository, VotingRepository votingRepository,
                         VoteRepository voteRepository, DelegationRepository delegationRepository,
                         LoggingService loggingService, TopicRepository topicRepository,
                         MessageRepository messageRepository, MessageDetailsRepository messageDetailsRepository,
                         FeedbackRepository feedbackRepository, ParticipantRepository participantRepository,
                         VotingValidator votingValidator) {
        this.userRepository = userRepository;
        this.votingRepository = votingRepository;
        this.voteRepository = voteRepository;
        this.delegationRepository = delegationRepository;
        this.loggingService = loggingService;
        this.topicRepository = topicRepository;
        this.messageRepository = messageRepository;
        this.messageDetailsRepository = messageDetailsRepository;
        this.feedbackRepository = feedbackRepository;
        this.participantRepository = participantRepository;
        this.votingValidator = votingValidator;
    }

    @Transactional
    public void castVote(String username, Long votingId, List<String> voteChoices) throws ValidationException {
        UserEntity voter = userRepository.findByUsername(username)
                .orElseThrow(() -> new ValidationException(VOTER_NOT_FOUND));

        VotingEntity voting = votingRepository.findById(votingId)
                .orElseThrow(() -> new ValidationException(VOTING_NOT_FOUND));

        votingValidator.validateVotingDates(voting);

        votingValidator.validateParticipation(voting, voter);

        votingValidator.validateChoice(voteChoices, voting);

        votingValidator.checkIfAlreadyVoted(voter, voting);

        votingValidator.checkIfDelegationExists(voter, voting);

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

        loggingService.log(voter, Action.DIRECT_VOTE, "Ο χρήστης " + voter.getUsername() + " ψήφισε για την ψηφοφορία " + votingId + ".");

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

                loggingService.log(finalDelegate, Action.DELEGATED_VOTE,
                        "Ο χρήστης " + delegate.getUsername() + " ψήφισε για την ψηφοφορία " + voting.getId() +
                                " εκ μέρους του " + delegation.getDelegator().getUsername() + ".");
            }

            castDelegatedVote(delegation.getDelegator(), voting, votingOptions, finalDelegate);
        }
    }

    @Transactional
    public void initializeVoting(String username, VotingInitializationDto votingInitializationDto) throws ValidationException, VotingCreationException {
        votingValidator.validateVotingInitialization(votingInitializationDto);

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ValidationException(USER_NOT_FOUND));

        TopicEntity topic = topicRepository.findByTitle(votingInitializationDto.topic())
                .orElseThrow(() -> new ValidationException("Η θεματική περιοχή δεν βρέθηκε"));

        votingValidator.checkIfNameExists(votingInitializationDto.name());

        Set<UserEntity> committee = votingValidator.checkCommittee(votingInitializationDto.committee());

        committee.forEach(member -> {
            if (member.getRoles().add(Role.ELECTORAL_COMMITTEE)) {
                loggingService.log(user, Action.NEW_ROLE, "Ο ρόλος + " + Role.ELECTORAL_COMMITTEE + " δόθηκε από το χρήστη " +
                        username + " στο χρήστη " + member.getUsername() + " αυτόματα, ως μέρος της διαδικασίας δημιουργίας ψηφοφορίας.");
            }
            userRepository.save(member);
        });

        VotingEntity votingEntity = new VotingEntity(votingInitializationDto.name(), topic, committee);

        votingRepository.save(votingEntity);

        loggingService.log(user, Action.VOTING_CREATION, "Ο χρήστης " + user.getUsername() + " δημιούργησε την ψηφοφορία " +
                votingEntity.getId() + " με τίτλο " + votingInitializationDto.name() + " στη θεματική περιοχή " + topic.getTitle() + ".");
    }

    @Transactional
    public void editVoting(String username, VotingCreationDto votingCreationDto) throws ValidationException, AuthorizationException {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ValidationException(USER_NOT_FOUND));

        VotingEntity voting = votingRepository.findById(votingCreationDto.id())
                .orElseThrow(() -> new ValidationException(VOTING_NOT_FOUND));

        votingValidator.checkAuthorizationToEdit(voting, user);

        boolean mandatory = false;

        if (votingCreationDto.startDate() != null) {
            mandatory = true;
            LocalDate startDate = votingValidator.validateStartDate(votingCreationDto.startDate(), voting.getStartDate());
            voting.setStartDate(startDate);
        }

        if (mandatory) {
            votingValidator.validateMandatoryFields(votingCreationDto);
        }

        if (votingCreationDto.endDate() != null) {
            LocalDate endDate = votingValidator.validateEndDate(votingCreationDto.endDate(), voting.getEndDate(), voting.getStartDate());
            voting.setEndDate(endDate);
        }

        String info = votingValidator.validateInformation(votingCreationDto.description());
        voting.setInformation(info);

        if (votingCreationDto.mechanism() != null) {
            VotingType type = votingValidator.validateVotingMechanism(votingCreationDto.mechanism());
            voting.setVotingType(type);
        }

        if (votingCreationDto.options() != null && !votingCreationDto.options().isEmpty()) {
            votingValidator.validateVotingOptions(votingCreationDto.options());
            voting.clearVotingOptions();
            votingCreationDto.options().forEach(option ->
                    voting.addVotingOption(option.title(), option.details()));
        }

        if (votingCreationDto.voteLimit() != null) {
            Integer limit = votingValidator.validateOptionsAndLimit(votingCreationDto.options(), voting.getVotingType(), votingCreationDto.voteLimit());
            voting.setVoteLimit(limit);
        } else {
            voting.setVoteLimit(null);
        }

        votingRepository.save(voting);

        loggingService.log(user, Action.VOTING_EDIT, "Ο χρήστης " + user.getUsername() + " επεξεργάστηκε την ψηφοφορία " +
                voting.getId() + " με τίτλο " + voting.getName() + " στη θεματική περιοχή " + voting.getTopic().getTitle() + ".");
    }

    @Transactional
    public void requestAccess(String username, Long votingId) throws ValidationException {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ValidationException(VOTER_NOT_FOUND));

        VotingEntity voting = votingRepository.findById(votingId)
                .orElseThrow(() -> new ValidationException(VOTING_NOT_FOUND));

        votingValidator.checkIfRequestExists(user, voting);

        votingValidator.validateVotingDatesForRequest(voting.getStartDate(), voting.getEndDate());

        ParticipantEntity participantEntity = new ParticipantEntity(user, voting);
        participantRepository.save(participantEntity);

        loggingService.log(user, Action.REQUEST_CREATION,
                "Ο χρήστης " + username + " δημιούργησε αίτημα συμμετοχής στην ψηφοφορία με τίτλο " +
                        voting.getName() + ".");
    }

    public List<ParticipationRequestDto> getRequests(String username, Long votingId) throws ValidationException, AuthorizationException {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ValidationException(USER_NOT_FOUND));

        VotingEntity voting = votingRepository.findById(votingId)
                .orElseThrow(() -> new ValidationException(VOTING_NOT_FOUND));

        votingValidator.validateVotingDatesForGettingRequests(voting.getStartDate(), voting.getEndDate());

        votingValidator.checkAuthorizationToEdit(voting, user);

        return participantRepository.findByVotingAndStatusIs(voting, null).stream().map(p ->
                new ParticipationRequestDto(p.getId().intValue(), p.getUser().getName(), p.getUser().getSurname(),
                        p.getUser().getUsername())).toList();
    }

    @Transactional
    public void processRequest(String username, Long requestId, boolean approve) throws ValidationException, AuthorizationException {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ValidationException(USER_NOT_FOUND));

        ParticipantEntity participant = participantRepository.findById(requestId)
                .orElseThrow(() -> new ValidationException("Το αίτημα δεν βρέθηκε"));

        if (participant.getStatus() != null) {
            throw new ValidationException("Το αίτημα είναι ήδη επεξεργασμένο");
        }

        VotingEntity voting = participant.getVoting();

        votingValidator.checkAuthorizationToEdit(voting, user);

        participant.setStatus(approve);
        participantRepository.save(participant);

        if (approve) {
            loggingService.log(user, Action.REQUEST_APPROVAL,
                    "Το αίτημα του χρήστη " + participant.getUser().getUsername() + " για συμμετοχή στην ψηφοφορία με τίτλο " +
                            voting.getName() + " έγινε δεκτό από το χρήστη " + username + ".");
        } else {
            loggingService.log(user, Action.REQUEST_REJECTION,
                    "Το αίτημα του χρήστη " + participant.getUser().getUsername() + " για συμμετοχή στην ψηφοφορία με τίτλο " +
                            voting.getName() + " απορρίφθηκε από το χρήστη " + username + ".");
        }
    }

    public VotingAccessDto hasAccess(String username, Long votingId) throws ValidationException {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ValidationException(VOTER_NOT_FOUND));

        VotingEntity voting = votingRepository.findById(votingId)
                .orElseThrow(() -> new ValidationException(VOTING_NOT_FOUND));

        if (voting.getElectoralCommittee().contains(user)) {
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
                .orElseThrow(() -> new ValidationException(VOTER_NOT_FOUND));

        List<VotingEntity> votingEntities = votingRepository.findAll();

        if (Role.ELECTORAL_COMMITTEE.equals(selectedRole)) {
            return votingEntities.stream().filter(v -> v.getElectoralCommittee().contains(voter)).map(v -> {
                boolean hasVoted = v.getVotes().stream().anyMatch(vote -> vote.getOriginalVoter().getId().equals(voter.getId()));
                return new VotingDto(v.getName(), v.getTopic().getTitle(),
                        DateHelper.toString(v.getStartDate()), DateHelper.toString(v.getEndDate()), hasVoted, v.getVotes().size(),
                        v.getId().intValue());
            }).toList();
        }

        return votingEntities.stream().filter(v -> v.getStartDate() != null).map(v -> {
            boolean hasVoted = v.getVotes().stream().anyMatch(vote -> vote.getOriginalVoter().getId().equals(voter.getId()));
            return new VotingDto(v.getName(), v.getTopic().getTitle(),
                    DateHelper.toString(v.getStartDate()), DateHelper.toString(v.getEndDate()), hasVoted, v.getVotes().size(),
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
                .orElseThrow(() -> new ValidationException(VOTING_NOT_FOUND));

        UserEntity voter = userRepository.findByUsername(username)
                .orElseThrow(() -> new ValidationException(VOTER_NOT_FOUND));

        checkVotingAccess(username, votingId); //TODO: Clarify if everyone has access after is expires

        if (voting.getStartDate() != null && voting.getEndDate() != null && !voting.getEndDate().isAfter(LocalDate.now())) {
            return getInactiveVotingStatistics(voting, voter);
        }

        if (voting.getStartDate() == null) {
            if (voting.getElectoralCommittee().contains(voter)) {
                return getVotingPreviewDetails(voting);
            }
            throw new AuthorizationException("Δεν ανήκετε στην εφορευτική επιτροπή αυτής της ψηφοφορίας");
        }

        return getActiveVotingDetails(voting, voter);
    }

    public List<VotingTitleDto> getVotingTitles() {
        return votingRepository.findAll().stream().filter(v -> v.getStartDate() != null)
                .map(votingEntity -> new VotingTitleDto(votingEntity.getId().intValue(), votingEntity.getName())).toList();
    }

    public List<DiscussionDto> getDiscussions(String username, Long votingId) throws ValidationException, AuthorizationException {
        VotingEntity voting = votingRepository.findById(votingId)
                .orElseThrow(() -> new ValidationException(VOTING_NOT_FOUND));

        votingValidator.validateHasNotExpired(voting.getEndDate());

        UserEntity voter = userRepository.findByUsername(username)
                .orElseThrow(() -> new ValidationException(USER_NOT_FOUND));

        checkVotingAccess(username, votingId);

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

    @Transactional
    public void reactToMessage(Long messageId, String username, boolean action) throws ValidationException, AuthorizationException {
        MessageEntity message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ValidationException("Το μήνυμα δεν βρέθηκε"));

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ValidationException(USER_NOT_FOUND));

        votingValidator.validateHasNotExpired(message.getVoting().getEndDate());

        checkVotingAccess(username, message.getVoting().getId());

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

        loggingService.log(user, Action.REACTION,
                "Ο χρήστης " + username + " αντέδρασε στο μήνυμα " + messageId + " του χρήστη " +
                        message.getUser().getUsername() + " στην ψηφοφορία με τίτλο " + message.getVoting().getName() + ".");
    }

    @Transactional
    public void addComment(String username, Long votingId, String message) throws ValidationException, AuthorizationException {
        votingValidator.validateComment(message);

        VotingEntity voting = votingRepository.findById(votingId)
                .orElseThrow(() -> new ValidationException(VOTING_NOT_FOUND));

        votingValidator.validateHasNotExpired(voting.getEndDate());

        checkVotingAccess(username, votingId);

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ValidationException(USER_NOT_FOUND));

        voting.addMessage(message, user);
        votingRepository.save(voting);

        loggingService.log(user, Action.COMMENT,
                "Ο χρήστης " + username + " προσέθεσε σχόλιο στην ψηφοφορία με τίτλο " + voting.getName() + ".");
    }

    @Transactional
    public void addFeedback(String username, Long votingId, String message) throws ValidationException, AuthorizationException {
        votingValidator.validateFeedback(message);

        VotingEntity voting = votingRepository.findById(votingId)
                .orElseThrow(() -> new ValidationException(VOTING_NOT_FOUND));

        votingValidator.validateHasExpired(voting.getEndDate());

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ValidationException(USER_NOT_FOUND));

        checkVotingAccess(username, votingId);

        if (feedbackRepository.existsByVotingAndUser(voting, user)) {
            throw new ValidationException("Υπάρχει ήδη ανατροφοδότηση");
        }

        FeedbackEntity feedback = new FeedbackEntity(user, message, voting);
        feedbackRepository.save(feedback);

        loggingService.log(user, Action.FEEDBACK,
                "Ο χρήστης " + username + " προσέθεσε ανατροφοδότηση στην ψηφοφορία με τίτλο " + voting.getName() + ".");
    }

    public List<FeedbackDto> getFeedback(String username, Long votingId) throws ValidationException, AuthorizationException {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ValidationException(USER_NOT_FOUND));

        VotingEntity voting = votingRepository.findById(votingId)
                .orElseThrow(() -> new ValidationException(VOTING_NOT_FOUND));

        votingValidator.validateHasExpired(voting.getEndDate());

        votingValidator.checkAuthorizationToEdit(voting, user);

        return feedbackRepository.findByVoting(voting).stream().map(feedback ->
                new FeedbackDto(feedback.getContent())).toList();
    }

    private VotingDetailsDto getInactiveVotingStatistics(VotingEntity voting, UserEntity voter) {
        Map<VotingOptionDto, Integer> directResultMap = new HashMap<>();
        Map<VotingOptionDto, Integer> delegatedResultMap = new HashMap<>();
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
            directResultMap.put(votingOptionDto, 0);
            delegatedResultMap.put(votingOptionDto, 0);
        });

        List<VoteEntity> votes = voting.getVotes();
        votes.forEach(v -> {
            List<VoteDetailsEntity> voteDetailsEntities = v.getVoteDetails();
            voteDetailsEntities.forEach(details -> {
                VotingOptionDto votingOptionDto = new VotingOptionDto(details.getVotingOption().getName(), details.getVotingOption().getDescription());
                if (v.getVoter() == null) {
                    directResultMap.merge(votingOptionDto, 1, Integer::sum);
                } else {
                    delegatedResultMap.merge(votingOptionDto, 1, Integer::sum);
                }
            });

            if (v.getVoter() != null) {
                delegatedVotes.incrementAndGet();
            } else {
                directVotes.incrementAndGet();
            }
        });

        List<VotingResultDto> resultList = getVotingResultDtos(directResultMap, delegatedResultMap);

        Optional<FeedbackEntity> byVotingAndUser = feedbackRepository.findByVotingAndUser(voting, voter);
        String feedback = byVotingAndUser.map(FeedbackEntity::getContent).orElse(null);

        List<VotingOptionDto> userOptionsList = null;
        if (!userOptions.isEmpty()) {
            userOptionsList = new ArrayList<>(userOptions);
        }

        return new VotingDetailsDto(voting.getName(), voting.getTopic().getTitle(),
                DateHelper.toString(voting.getStartDate()), DateHelper.toString(voting.getEndDate()), voting.getInformation(),
                delegated, voting.getVotingType().getId(), voting.getVoteLimit(), resultList, userOptionsList, directVotes.get(), delegatedVotes.get(), feedback);
    }

    private List<VotingResultDto> getVotingResultDtos(Map<VotingOptionDto, Integer> directResultMap, Map<VotingOptionDto, Integer> delegatedResultMap) {
        Map<VotingOptionDto, VotingResultDto> results = new HashMap<>();

        for (Map.Entry<VotingOptionDto, Integer> entry : directResultMap.entrySet()) {
            results.put(entry.getKey(), new VotingResultDto(entry.getKey(), entry.getValue(), 0));
        }

        for (Map.Entry<VotingOptionDto, Integer> entry : delegatedResultMap.entrySet()) {
            results.merge(entry.getKey(),
                    new VotingResultDto(entry.getKey(), 0, entry.getValue()),
                    (existing, newValue) -> new VotingResultDto(
                            existing.option(),
                            existing.directVotes() + newValue.directVotes(),
                            existing.delegatedVotes() + newValue.delegatedVotes()
                    ));
        }

        return new ArrayList<>(results.values());
    }

    private VotingDetailsDto getVotingPreviewDetails(VotingEntity voting) {
        List<VotingOptionsEntity> votingOptions = voting.getVotingOptions();
        List<VotingResultDto> votingResults = new ArrayList<>();

        if (votingOptions != null) {
            votingResults = votingOptions.stream().map(option ->
                    new VotingResultDto(new VotingOptionDto(option.getName(), option.getDescription()), null, null)).toList();
        }

        Integer votingTypeId = voting.getVotingType() == null ? null : voting.getVotingType().getId();

        return new VotingDetailsDto(voting.getName(), voting.getTopic().getTitle(),
                DateHelper.toString(voting.getStartDate()), DateHelper.toString(voting.getEndDate()), voting.getInformation(),
                null, votingTypeId, voting.getVoteLimit(), votingResults, null, null,
                null, null);
    }

    private VotingDetailsDto getActiveVotingDetails(VotingEntity voting, UserEntity voter) {
        Optional<VoteEntity> voteEntityOptional = voteRepository.findByOriginalVoterAndVoting(voter, voting);
        List<VotingOptionsEntity> votingOptions = voting.getVotingOptions();
        List<VotingResultDto> votingResults = votingOptions.stream().map(option ->
                new VotingResultDto(new VotingOptionDto(option.getName(), option.getDescription()), null, null)).toList();

        if (voteEntityOptional.isPresent()) {
            VoteEntity vote = voteEntityOptional.get();
            List<VoteDetailsEntity> voteDetailsEntities = vote.getVoteDetails();
            List<VotingOptionDto> votingOptionDtos = voteDetailsEntities.stream().map(voteDetailsEntity ->
                    new VotingOptionDto(voteDetailsEntity.getVotingOption().getName(), voteDetailsEntity.getVotingOption().getDescription())).toList();

            return new VotingDetailsDto(voting.getName(), voting.getTopic().getTitle(),
                    DateHelper.toString(voting.getStartDate()), DateHelper.toString(voting.getEndDate()), voting.getInformation(),
                    vote.getVoter() != null, voting.getVotingType().getId(), voting.getVoteLimit(), votingResults, votingOptionDtos, null,
                    null, null);
        }

        return new VotingDetailsDto(voting.getName(), voting.getTopic().getTitle(),
                DateHelper.toString(voting.getStartDate()), DateHelper.toString(voting.getEndDate()), voting.getInformation(),
                null, voting.getVotingType().getId(), voting.getVoteLimit(), votingResults, null, null,
                null, null);
    }

    private void checkVotingAccess(String username, Long votingId) throws ValidationException, AuthorizationException {
        VotingAccessDto votingAccessDto = hasAccess(username, votingId);
        if (!votingAccessDto.isPresent() || !Boolean.TRUE.equals(votingAccessDto.hasAccess())) {
            throw new AuthorizationException("Δεν έχετε πρόσβαση σε αυτή την ψηφοφορία");
        }
    }
}
