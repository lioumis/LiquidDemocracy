package gr.upatras.ceid.ld.service;

import gr.upatras.ceid.ld.dto.*;
import gr.upatras.ceid.ld.entity.*;
import gr.upatras.ceid.ld.enums.Action;
import gr.upatras.ceid.ld.exception.ValidationException;
import gr.upatras.ceid.ld.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public VotingService(UserRepository userRepository, VotingRepository votingRepository, VoteRepository voteRepository, DelegationRepository delegationRepository, AuditLogRepository auditLogRepository) {
        this.userRepository = userRepository;
        this.votingRepository = votingRepository;
        this.voteRepository = voteRepository;
        this.delegationRepository = delegationRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void castVote(Long voterId, Long votingId, String voteChoice) throws ValidationException {
        UserEntity voter = userRepository.findById(voterId)
                .orElseThrow(() -> new ValidationException("Voter not found"));
        VotingEntity voting = votingRepository.findById(votingId)
                .orElseThrow(() -> new ValidationException("Voting not found"));

        if (voteRepository.existsByVoterAndVoting(voter, voting)) {
//            throw new ValidationException("Ο χρήστης έχει ήδη ψηφίσει για αυτό το θέμα.");
            return;
        }

        if (voting.getEndDate().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Η ψηφοφορία έχει λήξει.");
        }

        Optional<DelegationEntity> delegationOpt = delegationRepository.findByDelegatorAndTopic(voter, voting.getTopic());
        if (delegationOpt.isPresent()) {
            throw new ValidationException("Ο χρήστης έχει ήδη αναθέσει την ψήφο του σε άλλο χρήστη και δεν μπορεί να ψηφίσει άμεσα.");
        }

        List<VotingOptionsEntity> votingOptions = voting.getVotingOptions();
        VotingOptionsEntity optionEntity = votingOptions.stream().filter(option -> voteChoice.equals(option.getName())).findFirst()
                .orElseThrow(() -> new ValidationException("Option not found"));

        VoteDetailsEntity voteDetailsEntity = new VoteDetailsEntity(1, optionEntity);

        List<VoteDetailsEntity> voteDetailsEntities = new ArrayList<>();
        voteDetailsEntities.add(voteDetailsEntity);
        VoteEntity vote = new VoteEntity(voter, voting, false, voteDetailsEntities);
        voteRepository.save(vote);

        AuditLogEntity auditLog = new AuditLogEntity(voter, Action.DIRECT_VOTE, "Ο χρήστης " + voter.getUsername() + " ψήφισε για την ψηφοφορία " + votingId + ".");
        auditLogRepository.save(auditLog);

        castDelegatedVote(voter, voting, voteChoice);
    }

    public void castDelegatedVote(UserEntity delegate, VotingEntity voting, String voteChoice) throws ValidationException {
        List<DelegationEntity> delegations = delegationRepository.findByDelegateAndTopic(delegate, voting.getTopic());

        if (delegations.isEmpty()) {
//            throw new ValidationException("Δεν βρέθηκε ανάθεση ψήφου για το συγκεκριμένο θέμα.");
            return;
        }

        for (DelegationEntity delegation : delegations) {
            if (voteRepository.existsByVoterAndVoting(delegation.getDelegator(), voting)) {
                throw new ValidationException("Η ψήφος έχει ήδη καταχωρηθεί για τον χρήστη " + delegation.getDelegator().getUsername());
            } //TODO: This checks if A -> B and A has already voted. It doesn't check if A -> B -> C and A has already voted!

            //TODO: Add check in the delegation process that a vote has not already been cast by the delegating user.
            //TODO: What if a user delegates a vote to someone that has already voted? An automatic vote should happen for every vote delegated.
            //TODO: If a user removes the delegation and votes directly, the whole delegation chain has to be removed as well as the last vote if exists.
            //TODO: Clarify what a topic is. Is it something general that can contain many votings? And a vote can be delegated for

            List<VotingOptionsEntity> votingOptions = voting.getVotingOptions();
            VotingOptionsEntity optionEntity = votingOptions.stream().filter(option -> voteChoice.equals(option.getName())).findFirst()
                    .orElseThrow(() -> new ValidationException("Option not found"));

            VoteDetailsEntity voteDetailsEntity = new VoteDetailsEntity(1, optionEntity);

            List<VoteDetailsEntity> voteDetailsEntities = new ArrayList<>();
            voteDetailsEntities.add(voteDetailsEntity);
            VoteEntity vote = new VoteEntity(delegate, voting, true, voteDetailsEntities);
            voteRepository.save(vote);

            AuditLogEntity auditLog = new AuditLogEntity(delegate, Action.DELEGATED_VOTE,
                    "Ο χρήστης " + delegate.getUsername() + " ψήφισε για την ψηφοφορία " + voting.getId() + " εκ μέρους του " + delegation.getDelegator().getUsername() + ".");
            auditLogRepository.save(auditLog);
        }
    }

    public List<VotingDto> getVotings(String username) throws ValidationException {
        UserEntity voter = userRepository.findByUsername(username)
                .orElseThrow(() -> new ValidationException("Voter not found"));

        List<VotingEntity> votingEntities = votingRepository.findAll();
        return votingEntities.stream().map(v -> { //TODO: Check if the user has voted indirectly
            boolean hasVoted = v.getVotes().stream().anyMatch(vote -> vote.getVoter().getId().equals(voter.getId()));
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
            return getInactiveVotingDetails(voting, voter);
        }

        return getActiveVotingStatistics(voting, voter);
    }

    private VotingDetailsDto getInactiveVotingDetails(VotingEntity voting, UserEntity voter) {
        Map<VotingOptionDto, Integer> resultMap = new HashMap<>();
        AtomicInteger directVotes = new AtomicInteger();
        AtomicInteger delegatedVotes = new AtomicInteger();
        VotingOptionDto userOption = null;
        Boolean delegated = null;
        Optional<VoteEntity> voteEntityOptional = voteRepository.findByVoterAndVoting(voter, voting);

        if (voteEntityOptional.isPresent()) {
            VoteEntity vote = voteEntityOptional.get();
            List<VoteDetailsEntity> voteDetails = vote.getVoteDetails();
            if (voteDetails != null && !voteDetails.isEmpty()) {
                VoteDetailsEntity voteDetailsEntity = voteDetails.get(0); //TODO: Support multiple choices
                VotingOptionsEntity votingOption = voteDetailsEntity.getVotingOption();
                userOption = new VotingOptionDto(votingOption.getName(), votingOption.getDescription());
            }
            delegated = vote.isDelegated();
        }

        List<VoteEntity> votes = voting.getVotes();
        votes.forEach(v -> {
            VoteDetailsEntity voteDetailsEntity = v.getVoteDetails().get(0); //TODO: Support multiple choices
            VotingOptionDto votingOptionDto = new VotingOptionDto(voteDetailsEntity.getVotingOption().getName(), voteDetailsEntity.getVotingOption().getDescription());
            resultMap.merge(votingOptionDto, 1, Integer::sum);

            if (v.isDelegated()) {
                delegatedVotes.incrementAndGet();
            } else {
                directVotes.incrementAndGet();
            }
        });

        List<VotingResultDto> results = resultMap.entrySet().stream().map(entry ->
                new VotingResultDto(entry.getKey(), entry.getValue())).toList();

        return new VotingDetailsDto(voting.getName(), voting.getTopic().getTitle(),
                toString(voting.getStartDate()), toString(voting.getEndDate()), voting.getInformation(),
                delegated, results, userOption, directVotes.get(), delegatedVotes.get());
    }

    private VotingDetailsDto getActiveVotingStatistics(VotingEntity voting, UserEntity voter) {
        return null; //TODO: Complete
    }

    private String toString(LocalDateTime localDateTime) {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(SIMPLE_DATE_FORMAT);
        return localDateTime.format(formatter);
    }
}
