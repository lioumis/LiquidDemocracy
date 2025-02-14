package gr.upatras.ceid.ld.common.auditlog.repository;

import gr.upatras.ceid.ld.common.auditlog.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Long> {
}