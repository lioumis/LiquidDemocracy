package gr.upatras.ceid.ld.delegation.validator;

import gr.upatras.ceid.ld.delegation.entity.DelegationEntity;
import gr.upatras.ceid.ld.voting.entity.ParticipantEntity;
import gr.upatras.ceid.ld.user.entity.UserEntity;
import gr.upatras.ceid.ld.voting.entity.VotingEntity;
import gr.upatras.ceid.ld.common.enums.Role;
import gr.upatras.ceid.ld.common.exception.ValidationException;
import gr.upatras.ceid.ld.delegation.repository.DelegationRepository;
import gr.upatras.ceid.ld.voting.repository.ParticipantRepository;
import gr.upatras.ceid.ld.voting.repository.VoteRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

@Component
public class DelegationValidator {
    private final DelegationRepository delegationRepository;

    private final VoteRepository voteRepository;

    private final ParticipantRepository participantRepository;

    public DelegationValidator(DelegationRepository delegationRepository, VoteRepository voteRepository, ParticipantRepository participantRepository) {
        this.delegationRepository = delegationRepository;
        this.voteRepository = voteRepository;
        this.participantRepository = participantRepository;
    }


    public void validateDelegate(Set<Role> roles) throws ValidationException {
        if (!roles.contains(Role.REPRESENTATIVE)) {
            throw new ValidationException("Ο χρήστης που επιλέξατε δεν είναι αντιπρόσωπος");
        }
    }

    public void validateSameUser(UserEntity delegator, UserEntity delegate) throws ValidationException {
        if (delegator.equals(delegate)) {
            throw new ValidationException("Δεν μπορείτε να αναθέσετε την ψήφο στον εαυτό σας");
        }
    }

    public void validateVotingIsActive(VotingEntity voting) throws ValidationException {
        if (voting.getEndDate().isBefore(LocalDate.now())) {
            throw new ValidationException("Η επιλεγμένη ψηφοφορία έχει λήξει.");
        }
    }

    public void validateDelegationExists(UserEntity delegator, VotingEntity voting) throws ValidationException {
        if (delegationRepository.existsByDelegatorAndVoting(delegator, voting)) {
            throw new ValidationException("Υπάρχει ήδη ανάθεση ψήφου για τη συγκεκριμένη ψηφοφορία.");
        }
    }

    public void validateVoteExists(UserEntity delegator, UserEntity delegate, VotingEntity voting) throws ValidationException {
        if (voteRepository.existsByOriginalVoterAndVoting(delegator, voting)) {
            throw new ValidationException("Έχετε ήδη ψηφίσει για τη συγκεκριμένη ψηφοφορία");
        }

        if (voteRepository.existsByOriginalVoterAndVoting(delegate, voting)) {
            throw new ValidationException("Ο επιλεγμένος αντιπρόσωπος έχει ήδη ψηφίσει για τη συγκεκριμένη ψηφοφορία");
        }
    }

    public void validateDelegatorAccessToVoting(VotingEntity voting, UserEntity delegator) throws ValidationException {
        if (!voting.getElectoralCommittee().contains(delegator)) {
            ParticipantEntity delegatorParticipantEntity = participantRepository.findByUserAndVoting(delegator, voting)
                    .orElseThrow(() -> new ValidationException("Δεν συμμετέχετε σε αυτή την ψηφοφορία"));

            if (delegatorParticipantEntity.getStatus() == null) {
                throw new ValidationException("Η συμμετοχή σας σε αυτή την ψηφοφορία δεν έχει εξεταστεί ακόμα");
            }

            if (Boolean.FALSE.equals(delegatorParticipantEntity.getStatus())) {
                throw new ValidationException("Η συμμετοχή σας σε αυτή την ψηφοφορία έχει απορριφθεί");
            }
        }
    }

    public void validateDelegateAccessToVoting(VotingEntity voting, UserEntity delegate) throws ValidationException {
        if (!voting.getElectoralCommittee().contains(delegate)) {
            ParticipantEntity delegateParticipantEntity = participantRepository.findByUserAndVoting(delegate, voting)
                    .orElseThrow(() -> new ValidationException("Ο αντιπρόσωπος που επιλέξατε δεν συμμετέχει σε αυτή την ψηφοφορία"));

            if (!Boolean.TRUE.equals(delegateParticipantEntity.getStatus())) {
                throw new ValidationException("Ο αντιπρόσωπος που επιλέξατε δεν συμμετέχει σε αυτή την ψηφοφορία");
            }
        }
    }

    public void validateNoCircleOccurs(UserEntity delegator, UserEntity delegate, VotingEntity voting) throws ValidationException {
        Optional<DelegationEntity> byDelegate = delegationRepository.findByDelegatorAndVoting(delegate, voting);

        if (byDelegate.isEmpty()) {
            return;
        }

        UserEntity currentDelegate = byDelegate.get().getDelegate();

        if (currentDelegate.equals(delegator)) {
            throw new ValidationException("Θα προκύψει κυκλική ανάθεση");
        }

        validateNoCircleOccurs(delegator, currentDelegate, voting);
    }
}
