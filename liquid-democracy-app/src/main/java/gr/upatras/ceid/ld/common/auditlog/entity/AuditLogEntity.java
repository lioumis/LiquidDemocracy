package gr.upatras.ceid.ld.common.auditlog.entity;

import gr.upatras.ceid.ld.common.converter.ActionConverter;
import gr.upatras.ceid.ld.common.enums.Action;
import gr.upatras.ceid.ld.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

@Entity(name = "audit_log")
@NoArgsConstructor
public class AuditLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Convert(converter = ActionConverter.class)
    private Action action;

    @Column(length = 1000)
    private String details;

    public AuditLogEntity(UserEntity user, Action action, String details) {
        this.user = user;
        this.action = action;
        this.details = details;
    }
}
