package gr.upatras.ceid.ld.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
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

    @OneToMany(mappedBy = "vote", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VoteDetailsEntity> voteDetails;

    public VoteEntity(UserEntity voter, VotingEntity voting, boolean delegated) {
        this.voter = voter;
        this.voting = voting;
        this.delegated = delegated;
    }

    public void addVoteDetails(VoteDetailsEntity detail) {
        if (voteDetails == null) {
            voteDetails = new ArrayList<>();
        }
        this.voteDetails.add(detail);
        detail.setVote(this);
    }
}
