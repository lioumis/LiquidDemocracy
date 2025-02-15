package gr.upatras.ceid.ld.delegation.service;

import gr.upatras.ceid.ld.common.auditlog.service.LoggingService;
import gr.upatras.ceid.ld.common.enums.Action;
import gr.upatras.ceid.ld.common.exception.ValidationException;
import gr.upatras.ceid.ld.delegation.dto.DelegationDto;
import gr.upatras.ceid.ld.delegation.dto.ReceivedDelegationDto;
import gr.upatras.ceid.ld.delegation.entity.DelegationEntity;
import gr.upatras.ceid.ld.delegation.repository.DelegationRepository;
import gr.upatras.ceid.ld.delegation.validator.DelegationValidator;
import gr.upatras.ceid.ld.user.entity.UserEntity;
import gr.upatras.ceid.ld.user.repository.UserRepository;
import gr.upatras.ceid.ld.voting.entity.VotingEntity;
import gr.upatras.ceid.ld.voting.repository.VotingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class DelegationService {
    private static final String USER_NOT_FOUND = "Ο χρήστης δεν βρέθηκε";

    private final DelegationValidator delegationValidator;

    private final DelegationRepository delegationRepository;

    private final LoggingService loggingService;

    private final UserRepository userRepository;

    private final VotingRepository votingRepository;

    public DelegationService(DelegationValidator delegationValidator, DelegationRepository delegationRepository,
                             LoggingService loggingService, UserRepository userRepository,
                             VotingRepository votingRepository) {
        this.delegationValidator = delegationValidator;
        this.delegationRepository = delegationRepository;
        this.loggingService = loggingService;
        this.userRepository = userRepository;
        this.votingRepository = votingRepository;
    }

    @Transactional
    public void delegateVote(String delegatorUsername, String delegateName, String delegateSurname, Long votingId) throws ValidationException {
        UserEntity delegator = userRepository.findByUsername(delegatorUsername)
                .orElseThrow(() -> new ValidationException(USER_NOT_FOUND));

        UserEntity delegate = userRepository.findByNameAndSurnameIgnoreCase(delegateName, delegateSurname)
                .orElseThrow(() -> new ValidationException("Ο αντιπρόσωπος δεν βρέθηκε"));

        delegationValidator.validateDelegate(delegate.getRoles());

        delegationValidator.validateSameUser(delegator, delegate);

        VotingEntity voting = votingRepository.findById(votingId)
                .orElseThrow(() -> new ValidationException("Η ψηφοφορία δεν βρέθηκε"));

        delegationValidator.validateVotingIsActive(voting);

        delegationValidator.validateDelegationExists(delegator, voting);

        delegationValidator.validateVoteExists(delegator, delegate, voting);

        delegationValidator.validateDelegatorAccessToVoting(voting, delegator);

        delegationValidator.validateDelegateAccessToVoting(voting, delegate);

        delegationValidator.validateNoCircleOccurs(delegator, delegate, voting);

        DelegationEntity delegation = new DelegationEntity(voting, delegator, delegate);
        delegationRepository.save(delegation);

        loggingService.log(delegator, Action.VOTE_DELEGATION,
                "Ο χρήστης " + delegator.getUsername() + " ανέθεσε την ψήφο του στον χρήστη " +
                        delegate.getUsername() + " για την ψηφοφορία " + votingId + ".");
    }

    public List<DelegationDto> getDelegations(String username) throws ValidationException {
        UserEntity delegator = userRepository.findByUsername(username)
                .orElseThrow(() -> new ValidationException(USER_NOT_FOUND));

        List<DelegationEntity> delegations = delegationRepository.findByDelegator(delegator);
        return delegations.stream().map(delegation -> {
            UserEntity delegate = delegation.getDelegate();
            return new DelegationDto(delegate.getName(), delegate.getSurname(), delegate.getUsername(), delegation.getVoting().getName());
        }).toList();
    }

    public List<ReceivedDelegationDto> getReceivedDelegations(String username) throws ValidationException {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ValidationException(USER_NOT_FOUND));

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
}
