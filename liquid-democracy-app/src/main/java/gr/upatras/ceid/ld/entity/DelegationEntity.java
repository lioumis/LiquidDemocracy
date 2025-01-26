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
    @JoinColumn(name = "voting_id")
    private VotingEntity voting;

    public DelegationEntity(VotingEntity voting, UserEntity delegator, UserEntity delegate) {
        this.voting = voting;
        this.delegator = delegator;
        this.delegate = delegate;
    }
}
