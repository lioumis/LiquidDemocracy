package gr.upatras.ceid.ld.repository;

import gr.upatras.ceid.ld.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Long> {
}