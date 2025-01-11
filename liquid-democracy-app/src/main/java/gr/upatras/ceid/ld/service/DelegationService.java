package gr.upatras.ceid.ld.service;

import gr.upatras.ceid.ld.dto.DelegationDto;
import gr.upatras.ceid.ld.dto.ReceivedDelegationDto;
import gr.upatras.ceid.ld.entity.AuditLogEntity;
import gr.upatras.ceid.ld.entity.DelegationEntity;
import gr.upatras.ceid.ld.entity.TopicEntity;
import gr.upatras.ceid.ld.entity.UserEntity;
import gr.upatras.ceid.ld.enums.Action;
import gr.upatras.ceid.ld.exception.ValidationException;
import gr.upatras.ceid.ld.repository.AuditLogRepository;
import gr.upatras.ceid.ld.repository.DelegationRepository;
import gr.upatras.ceid.ld.repository.TopicRepository;
import gr.upatras.ceid.ld.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class DelegationService {
    private final DelegationRepository delegationRepository;

    private final AuditLogRepository auditLogRepository;

    private final TopicRepository topicRepository;

    private final UserRepository userRepository;

    public DelegationService(DelegationRepository delegationRepository, AuditLogRepository auditLogRepository, TopicRepository topicRepository, UserRepository userRepository) {
        this.delegationRepository = delegationRepository;
        this.auditLogRepository = auditLogRepository;
        this.topicRepository = topicRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void delegateVote(String delegatorUsername, String delegateName, String delegateSurname, Long topicId) throws ValidationException {
        UserEntity delegator = userRepository.findByUsername(delegatorUsername)
                .orElseThrow(() -> new ValidationException("Ο χρήστης δεν βρέθηκε"));

        UserEntity delegate = userRepository.findByNameAndSurnameIgnoreCase(delegateName, delegateSurname)
                .orElseThrow(() -> new ValidationException("Ο αντιπρόσωπος δεν βρέθηκε"));

        TopicEntity topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ValidationException("Το θέμα δεν βρέθηκε"));

        if (delegator.equals(delegate)) {
            throw new ValidationException("Δεν μπορείτε να αναθέσετε την ψήφο στον εαυτό σας");
        }

        if (delegationRepository.existsByDelegatorAndTopic(delegator, topic)) {
            throw new ValidationException("Υπάρχει ήδη ανάθεση ψήφου για το συγκεκριμένο θέμα.");
        }

        checkForCircularDelegation(delegator, delegate, topic);

        DelegationEntity delegation = new DelegationEntity(topic, delegator, delegate);
        delegationRepository.save(delegation);

        AuditLogEntity auditLog = new AuditLogEntity(delegator, Action.VOTE_DELEGATION,
                "Ο χρήστης " + delegator.getId() + " ανέθεσε την ψήφο του στον χρήστη " + delegate.getId() + " για το θέμα " + topicId + ".");
        auditLogRepository.save(auditLog);
    }

    public List<DelegationDto> getDelegations(String username) throws ValidationException {
        UserEntity delegator = userRepository.findByUsername(username)
                .orElseThrow(() -> new ValidationException("Ο χρήστης δεν βρέθηκε"));

        List<DelegationEntity> delegations = delegationRepository.findByDelegator(delegator);
        return delegations.stream().map(delegation -> {
            UserEntity delegate = delegation.getDelegate();
            return new DelegationDto(delegate.getName(), delegate.getSurname(), delegate.getUsername(), delegation.getTopic().getTitle());
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

            String topicTitle = currentDelegation.getTopic().getTitle();
            groupedDelegations.merge(topicTitle, 1, Integer::sum);

            List<DelegationEntity> nextDelegations = delegationRepository.findByDelegateAndTopic(
                    currentDelegation.getDelegator(),
                    currentDelegation.getTopic()
            );
            delegationQueue.addAll(nextDelegations);
        }

        return groupedDelegations.entrySet().stream()
                .map(entry -> new ReceivedDelegationDto(entry.getKey(), entry.getValue()))
                .toList();
    }

    @Transactional
    public void removeDelegation(Long delegatorId, Long topicId) throws ValidationException {
        UserEntity delegator = userRepository.findById(delegatorId)
                .orElseThrow(() -> new ValidationException("Ο χρήστης δεν βρέθηκε"));
        TopicEntity topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ValidationException("Το θέμα δεν βρέθηκε"));

        DelegationEntity delegation = delegationRepository.findByDelegatorAndTopic(delegator, topic)
                .orElseThrow(() -> new ValidationException("Δεν βρέθηκε ανάθεση για το θέμα από εσάς"));
        delegationRepository.delete(delegation);

        AuditLogEntity auditLog = new AuditLogEntity(delegator, Action.VOTE_DELEGATION_REMOVAL,
                "Ο χρήστης " + delegatorId + " αφαίρεσε την ανάθεση της ψήφου του για το θέμα " + topicId + ".");
        auditLogRepository.save(auditLog);
    }

    private void checkForCircularDelegation(UserEntity delegator, UserEntity delegate, TopicEntity topic) throws ValidationException {
        Optional<DelegationEntity> byDelegate = delegationRepository.findByDelegatorAndTopic(delegate, topic);

        if (byDelegate.isEmpty()) {
            return;
        }

        UserEntity currentDelegate = byDelegate.get().getDelegate();

        if (currentDelegate.equals(delegator)) {
            throw new ValidationException("Θα προκύψει κυκλική ανάθεση");
        }

        checkForCircularDelegation(delegator, currentDelegate, topic);
    }
}
