package gr.upatras.ceid.ld.service;

import gr.upatras.ceid.ld.dto.SuggestedVotingDto;
import gr.upatras.ceid.ld.entity.*;
import gr.upatras.ceid.ld.enums.Action;
import gr.upatras.ceid.ld.exception.ValidationException;
import gr.upatras.ceid.ld.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class VotingService {

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

        Optional<DelegationEntity> delegationOpt = delegationRepository.findByDelegatorAndTopic(voter, voting.getTopic());
        if (delegationOpt.isPresent()) {
            throw new ValidationException("Ο χρήστης έχει ήδη αναθέσει την ψήφο του σε άλλο χρήστη και δεν μπορεί να ψηφίσει άμεσα.");
        }

        VoteEntity vote = new VoteEntity(voter, voting, voteChoice, false);
//        vote.setDelegated(false); //TODO: Find a way to distinguish the delegated votes.
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

            VoteEntity vote = new VoteEntity(delegate, voting, voteChoice, true);
            voteRepository.save(vote);

            AuditLogEntity auditLog = new AuditLogEntity(delegate, Action.DELEGATED_VOTE,
                    "Ο χρήστης " + delegate.getUsername() + " ψήφισε για την ψηφοφορία " + voting.getId() + " εκ μέρους του " + delegation.getDelegator().getUsername() + ".");
            auditLogRepository.save(auditLog);
        }
    }

    public List<SuggestedVotingDto> getSuggestedVotings() throws ValidationException {
        Pageable pageable = PageRequest.of(0, 6);
        List<VotingEntity> topVotings = votingRepository.findTopVotingsWithMostVotesAndComments(pageable);
        return topVotings.stream().map(v -> new SuggestedVotingDto(v.getName(), v.getTopic().getTitle(),
                v.getVotes().size(), v.getMessages().size(), v.getId().intValue())).toList();
    }
}
