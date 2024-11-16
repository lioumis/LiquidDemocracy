package gr.upatras.ceid.ld.entity;

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

    private String action; //TODO: Enum

    private String details;

    public AuditLogEntity(UserEntity user, String action, String details) {
        this.user = user;
        this.action = action;
        this.details = details;
    }
}
