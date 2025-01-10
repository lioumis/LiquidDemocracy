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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
                .orElseThrow(() -> new ValidationException("Delegator not found"));

        UserEntity delegate = userRepository.findByNameAndSurnameIgnoreCase(delegateName, delegateSurname)
                .orElseThrow(() -> new ValidationException("Delegate not found"));

        TopicEntity topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ValidationException("Topic not found"));

        if (delegationRepository.existsByDelegatorAndTopic(delegator, topic)) {
            throw new ValidationException("Υπάρχει ήδη ανάθεση ψήφου για το συγκεκριμένο θέμα.");
        }

        DelegationEntity delegation = new DelegationEntity(new TopicEntity(topicId), delegator, delegate);
        delegationRepository.save(delegation);

        AuditLogEntity auditLog = new AuditLogEntity(delegator, Action.VOTE_DELEGATION,
                "Ο χρήστης " + delegator.getId() + " ανέθεσε την ψήφο του στον χρήστη " + delegate.getId() + " για το θέμα " + topicId + ".");
        auditLogRepository.save(auditLog);
    }

    public List<DelegationDto> getDelegations(String username) throws ValidationException {
        UserEntity delegator = userRepository.findByUsername(username)
                .orElseThrow(() -> new ValidationException("Delegator not found"));

        List<DelegationEntity> delegations = delegationRepository.findByDelegator(delegator);
        return delegations.stream().map(delegation -> {
            UserEntity delegate = delegation.getDelegate();
            return new DelegationDto(delegate.getName(), delegate.getSurname(), delegate.getUsername(), delegation.getTopic().getTitle());
        }).toList();
    }

    public List<ReceivedDelegationDto> getReceivedDelegations(String username) throws ValidationException {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ValidationException("User not found"));

        List<DelegationEntity> delegations = delegationRepository.findByDelegate(user);

        Map<String, Integer> groupedDelegations = delegations.stream()
                .collect(Collectors.groupingBy(
                        delegation -> delegation.getTopic().getTitle(),
                        Collectors.summingInt(delegation -> 1)
                ));

        return groupedDelegations.entrySet().stream()
                .map(entry -> new ReceivedDelegationDto(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void removeDelegation(Long delegatorId, Long topicId) throws ValidationException {
        UserEntity delegator = userRepository.findById(delegatorId)
                .orElseThrow(() -> new ValidationException("Delegator not found"));
        TopicEntity topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ValidationException("Topic not found"));

        DelegationEntity delegation = delegationRepository.findByDelegatorAndTopic(delegator, topic)
                .orElseThrow(() -> new ValidationException("No delegation found for this user and topic"));
        delegationRepository.delete(delegation);

        AuditLogEntity auditLog = new AuditLogEntity(delegator, Action.VOTE_DELEGATION_REMOVAL,
                "Ο χρήστης " + delegatorId + " αφαίρεσε την ανάθεση της ψήφου του για το θέμα " + topicId + ".");
        auditLogRepository.save(auditLog);
    }
}
