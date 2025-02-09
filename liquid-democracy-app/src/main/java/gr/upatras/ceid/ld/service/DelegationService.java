package gr.upatras.ceid.ld.service;

import gr.upatras.ceid.ld.dto.DelegationDto;
import gr.upatras.ceid.ld.dto.ReceivedDelegationDto;
import gr.upatras.ceid.ld.entity.*;
import gr.upatras.ceid.ld.enums.Action;
import gr.upatras.ceid.ld.enums.Role;
import gr.upatras.ceid.ld.exception.ValidationException;
import gr.upatras.ceid.ld.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class DelegationService {
    private final DelegationRepository delegationRepository;

    private final AuditLogRepository auditLogRepository;

    private final UserRepository userRepository;

    private final VotingRepository votingRepository;

    private final VoteRepository voteRepository;

    private final ParticipantRepository participantRepository;

    public DelegationService(DelegationRepository delegationRepository, AuditLogRepository auditLogRepository,
                             UserRepository userRepository, VotingRepository votingRepository, VoteRepository voteRepository,
                             ParticipantRepository participantRepository) {
        this.delegationRepository = delegationRepository;
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
        this.votingRepository = votingRepository;
        this.voteRepository = voteRepository;
        this.participantRepository = participantRepository;
    }

    @Transactional
    public void delegateVote(String delegatorUsername, String delegateName, String delegateSurname, Long votingId) throws ValidationException {
        UserEntity delegator = userRepository.findByUsername(delegatorUsername)
                .orElseThrow(() -> new ValidationException("Ο χρήστης δεν βρέθηκε"));

        UserEntity delegate = userRepository.findByNameAndSurnameIgnoreCase(delegateName, delegateSurname)
                .orElseThrow(() -> new ValidationException("Ο αντιπρόσωπος δεν βρέθηκε"));

        //TODO: Move to Validator classes
        if (!delegate.getRoles().contains(Role.REPRESENTATIVE)) {
            throw new ValidationException("Ο χρήστης που επιλέξατε δεν είναι αντιπρόσωπος");
        }

        if (delegator.equals(delegate)) {
            throw new ValidationException("Δεν μπορείτε να αναθέσετε την ψήφο στον εαυτό σας");
        }

        VotingEntity voting = votingRepository.findById(votingId)
                .orElseThrow(() -> new ValidationException("Η ψηφοφορία δεν βρέθηκε"));

        if (voting.getEndDate().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Η επιλεγμένη ψηφοφορία έχει λήξει.");
        }

        if (delegationRepository.existsByDelegatorAndVoting(delegator, voting)) {
            throw new ValidationException("Υπάρχει ήδη ανάθεση ψήφου για τη συγκεκριμένη ψηφοφορία.");
        }

        if (voteRepository.existsByOriginalVoterAndVoting(delegator, voting)) {
            throw new ValidationException("Έχετε ήδη ψηφίσει για τη συγκεκριμένη ψηφοφορία");
        }

        if (voteRepository.existsByOriginalVoterAndVoting(delegate, voting)) {
            throw new ValidationException("Ο επιλεγμένος αντιπρόσωπος έχει ήδη ψηφίσει για τη συγκεκριμένη ψηφοφορία");
        }

        ParticipantEntity delegatorParticipantEntity = participantRepository.findByUserAndVoting(delegator, voting)
                .orElseThrow(() -> new ValidationException("Δεν συμμετέχετε σε αυτή την ψηφοφορία"));

        if (delegatorParticipantEntity.getStatus() == null) {
            throw new ValidationException("Η συμμετοχή σας σε αυτή την ψηφοφορία δεν έχει εξεταστεί ακόμα");
        }

        if (Boolean.FALSE.equals(delegatorParticipantEntity.getStatus())) {
            throw new ValidationException("Η συμμετοχή σας σε αυτή την ψηφοφορία έχει απορριφθεί");
        }

        ParticipantEntity delegateParticipantEntity = participantRepository.findByUserAndVoting(delegate, voting)
                .orElseThrow(() -> new ValidationException("Ο αντιπρόσωπος που επιλέξατε δεν συμμετέχει σε αυτή την ψηφοφορία"));

        if (!Boolean.TRUE.equals(delegateParticipantEntity.getStatus())) {
            throw new ValidationException("Ο αντιπρόσωπος που επιλέξατε δεν συμμετέχει σε αυτή την ψηφοφορία");
        }

        checkForCircularDelegation(delegator, delegate, voting);

        DelegationEntity delegation = new DelegationEntity(voting, delegator, delegate);
        delegationRepository.save(delegation);

        AuditLogEntity auditLog = new AuditLogEntity(delegator, Action.VOTE_DELEGATION,
                "Ο χρήστης " + delegator.getId() + " ανέθεσε την ψήφο του στον χρήστη " + delegate.getId() + " για την ψηφοφορία " + votingId + ".");
        auditLogRepository.save(auditLog);
    }

    public List<DelegationDto> getDelegations(String username) throws ValidationException {
        UserEntity delegator = userRepository.findByUsername(username)
                .orElseThrow(() -> new ValidationException("Ο χρήστης δεν βρέθηκε"));

        List<DelegationEntity> delegations = delegationRepository.findByDelegator(delegator);
        return delegations.stream().map(delegation -> {
            UserEntity delegate = delegation.getDelegate();
            return new DelegationDto(delegate.getName(), delegate.getSurname(), delegate.getUsername(), delegation.getVoting().getName());
        }).toList();
    }

    public List<ReceivedDelegationDto> getReceivedDelegations(String username) throws ValidationException {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ValidationException("Ο χρήστης δεν βρέθηκε"));

        List<DelegationEntity> directDelegations = delegationRepository.findByDelegate(user);

        Map<String, Integer> groupedDelegations = new HashMap<>();
        Set<Long> visitedDelegations = new HashSet<>();
        Queue<DelegationEntity> delegationQueue = new LinkedList<>(directDelegations);

        while (!delegationQueue.isEmpty()) {
            DelegationEntity currentDelegation = delegationQueue.poll();
            if (visitedDelegations.contains(currentDelegation.getId())) {
                continue;
            }
            visitedDelegations.add(currentDelegation.getId());

            String votingTitle = currentDelegation.getVoting().getName();
            groupedDelegations.merge(votingTitle, 1, Integer::sum);

            List<DelegationEntity> nextDelegations = delegationRepository.findByDelegateAndVoting(
                    currentDelegation.getDelegator(),
                    currentDelegation.getVoting()
            );
            delegationQueue.addAll(nextDelegations);
        }

        return groupedDelegations.entrySet().stream()
                .map(entry -> new ReceivedDelegationDto(entry.getKey(), entry.getValue()))
                .toList();
    }

    @Transactional
    public void removeDelegation(String username, Long votingId) throws ValidationException {
        UserEntity delegator = userRepository.findByUsername(username)
                .orElseThrow(() -> new ValidationException("Ο χρήστης δεν βρέθηκε"));
        VotingEntity voting = votingRepository.findById(votingId)
                .orElseThrow(() -> new ValidationException("Η ψηφοφορία δεν βρέθηκε"));

        DelegationEntity delegation = delegationRepository.findByDelegatorAndVoting(delegator, voting)
                .orElseThrow(() -> new ValidationException("Δεν βρέθηκε ανάθεση για την ψηφοφορία από εσάς"));
        delegationRepository.delete(delegation);

        AuditLogEntity auditLog = new AuditLogEntity(delegator, Action.VOTE_DELEGATION_REMOVAL,
                "Ο χρήστης " + username + " αφαίρεσε την ανάθεση της ψήφου του για το θέμα " + votingId + ".");
        auditLogRepository.save(auditLog);
    }

    private void checkForCircularDelegation(UserEntity delegator, UserEntity delegate, VotingEntity voting) throws ValidationException {
        Optional<DelegationEntity> byDelegate = delegationRepository.findByDelegatorAndVoting(delegate, voting);

        if (byDelegate.isEmpty()) {
            return;
        }

        UserEntity currentDelegate = byDelegate.get().getDelegate();

        if (currentDelegate.equals(delegator)) {
            throw new ValidationException("Θα προκύψει κυκλική ανάθεση");
        }

        checkForCircularDelegation(delegator, currentDelegate, voting);
    }
}
