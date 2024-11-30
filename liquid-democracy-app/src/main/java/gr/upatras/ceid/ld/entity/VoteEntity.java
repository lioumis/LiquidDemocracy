package gr.upatras.ceid.ld.entity;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Entity(name = "vote")
public class VoteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "voter_id")
    private UserEntity voter;

    @ManyToOne
    @JoinColumn(name = "voting_id")
    private VotingEntity voting;

    private String choice; //TODO: Enum

    @Column(nullable = false)
    private boolean delegated;

    public VoteEntity(UserEntity voter, VotingEntity voting, String choice, boolean delegated) {
        this.voter = voter;
        this.voting = voting;
        this.choice = choice;
        this.delegated = delegated;
    }
}
