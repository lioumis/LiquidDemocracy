package gr.upatras.ceid.ld.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
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

    @Column(nullable = false)
    private boolean delegated;

    @OneToMany(mappedBy = "vote")
    private List<VoteDetailsEntity> voteDetails;

    public VoteEntity(UserEntity voter, VotingEntity voting, boolean delegated, List<VoteDetailsEntity> voteDetails) {
        this.voter = voter;
        this.voting = voting;
        this.delegated = delegated;
        this.voteDetails = voteDetails;
    }
}
