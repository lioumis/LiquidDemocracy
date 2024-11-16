package gr.upatras.ceid.ld.service;

import gr.upatras.ceid.ld.entity.*;
import gr.upatras.ceid.ld.exception.ValidationException;
import gr.upatras.ceid.ld.repository.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VotingService {

    private final UserRepository userRepository;

    private final TopicRepository topicRepository;

    private final VoteRepository voteRepository;

    private final DelegationRepository delegationRepository;

    private final AuditLogRepository auditLogRepository;

    public VotingService(UserRepository userRepository, TopicRepository topicRepository, VoteRepository voteRepository, DelegationRepository delegationRepository, AuditLogRepository auditLogRepository) {
        this.userRepository = userRepository;
        this.topicRepository = topicRepository;
        this.voteRepository = voteRepository;
        this.delegationRepository = delegationRepository;
        this.auditLogRepository = auditLogRepository;
    }

    public void castVote(Long voterId, Long topicId, String voteChoice) throws ValidationException {
        UserEntity voter = userRepository.findById(voterId)
                .orElseThrow(() -> new ValidationException("Voter not found"));
        TopicEntity topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ValidationException("Topic not found"));

        if (voteRepository.existsByVoterAndTopic(voter, topic)) {
//            throw new ValidationException("Ο χρήστης έχει ήδη ψηφίσει για αυτό το θέμα.");
            return;
        }

        Optional<DelegationEntity> delegationOpt = delegationRepository.findByDelegatorAndTopic(voter, topic);
        if (delegationOpt.isPresent()) {
            throw new ValidationException("Ο χρήστης έχει ήδη αναθέσει την ψήφο του σε άλλο χρήστη και δεν μπορεί να ψηφίσει άμεσα.");
        }

        VoteEntity vote = new VoteEntity(voter, topic, voteChoice);
//        vote.setDelegated(false); //TODO: Find a way to distinguish the delegated votes.
        voteRepository.save(vote);

        AuditLogEntity auditLog = new AuditLogEntity(voter, "Άμεση Ψήφος", "Ο χρήστης " + voter.getUsername() + " ψήφισε για το θέμα " + topicId);
        auditLogRepository.save(auditLog);

        castDelegatedVote(voter, topic, voteChoice);
    }

    public void castDelegatedVote(UserEntity delegate, TopicEntity topic, String voteChoice) throws ValidationException {
        List<DelegationEntity> delegations = delegationRepository.findByDelegateAndTopic(delegate, topic);

        if (delegations.isEmpty()) {
//            throw new ValidationException("Δεν βρέθηκε ανάθεση ψήφου για το συγκεκριμένο θέμα.");
            return;
        }

        for (DelegationEntity delegation : delegations) {
            if (voteRepository.existsByVoterAndTopic(delegation.getDelegator(), topic)) {
                throw new ValidationException("Η ψήφος έχει ήδη καταχωρηθεί για τον χρήστη " + delegation.getDelegator().getUsername());
            } //TODO: This checks if A -> B and A has already voted. It doesn't check if A -> B -> C and A has already voted!

            //TODO: Add check in the delegation process that a vote has not already been cast by the delegating user.
            //TODO: What if a user delegates a vote to someone that has already voted? An automatic vote should happen for every vote delegated.
            //TODO: If a user removes the delegation and votes directly, the whole delegation chain has to be removed as well as the last vote if exists.
            //TODO: Clarify what a topic is. Is it something general that can contain many votings? And a vote can be delegated for

            VoteEntity vote = new VoteEntity(delegate, topic, voteChoice);
//            vote.setDelegated(true); // Σημειώνεται ως εξουσιοδοτημένη ψήφος TODO
            voteRepository.save(vote);

            AuditLogEntity auditLog = new AuditLogEntity(delegate, "Εξουσιοδοτημένη Ψήφος",
                    "Ο χρήστης " + delegate.getUsername() + " ψήφισε για το θέμα " + topic.getId() + " εκ μέρους του " + delegation.getDelegator().getUsername());
            auditLogRepository.save(auditLog);
        }
    }
}
