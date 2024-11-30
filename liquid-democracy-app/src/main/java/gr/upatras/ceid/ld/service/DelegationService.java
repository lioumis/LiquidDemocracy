package gr.upatras.ceid.ld.service;

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
    public void delegateVote(Long delegatorId, Long delegateId, Long topicId) throws ValidationException {
        UserEntity delegator = userRepository.findById(delegatorId)
                .orElseThrow(() -> new ValidationException("Delegator not found"));

        if (userRepository.findById(delegateId).isEmpty()) {
            throw new ValidationException("Delegate not found");
        }

        TopicEntity topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new ValidationException("Topic not found"));

        if (delegationRepository.existsByDelegatorAndTopic(delegator, topic)) {
            throw new ValidationException("Υπάρχει ήδη ανάθεση ψήφου για το συγκεκριμένο θέμα.");
        }

        DelegationEntity delegation = new DelegationEntity(new TopicEntity(topicId), new UserEntity(delegatorId), new UserEntity(delegateId));
        delegationRepository.save(delegation);

        AuditLogEntity auditLog = new AuditLogEntity(delegator, Action.VOTE_DELEGATION,
                "Ο χρήστης " + delegatorId + " ανέθεσε την ψήφο του στον χρήστη " + delegateId + " για το θέμα " + topicId + ".");
        auditLogRepository.save(auditLog);
    }

    @Transactional
    @Deprecated //TODO: Discuss if possible
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