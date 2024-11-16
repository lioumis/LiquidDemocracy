package gr.upatras.ceid.ld.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity(name = "delegation")
public class DelegationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "delegator_id")
    private UserEntity delegator;

    @ManyToOne
    @JoinColumn(name = "delegate_id")
    private UserEntity delegate;

    @ManyToOne
    @JoinColumn(name = "topic_id")
    private TopicEntity topic;

    public DelegationEntity(TopicEntity topic, UserEntity delegator, UserEntity delegate) {
        this.topic = topic;
        this.delegator = delegator;
        this.delegate = delegate;
    }
}
