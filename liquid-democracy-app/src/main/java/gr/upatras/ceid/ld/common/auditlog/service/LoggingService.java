package gr.upatras.ceid.ld.common.auditlog.service;

import gr.upatras.ceid.ld.common.auditlog.entity.AuditLogEntity;
import gr.upatras.ceid.ld.common.auditlog.repository.AuditLogRepository;
import gr.upatras.ceid.ld.common.enums.Action;
import gr.upatras.ceid.ld.user.entity.UserEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoggingService {
    private final AuditLogRepository auditLogRepository;


    public LoggingService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void log(UserEntity user, Action action, String message) {
        AuditLogEntity auditLog = new AuditLogEntity(user, action, message);
        auditLogRepository.save(auditLog);
    }
}
