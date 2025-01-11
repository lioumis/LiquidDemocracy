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
    @JoinColumn(name = "original_voter_id")
    private UserEntity originalVoter;

    @ManyToOne
    @JoinColumn(name = "voting_id")
    private VotingEntity voting;

    @OneToMany(mappedBy = "vote", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VoteDetailsEntity> voteDetails;

    public VoteEntity(UserEntity originalVoter, VotingEntity voting) {
        this.originalVoter = originalVoter;
        this.voting = voting;
    }

    public VoteEntity(UserEntity voter, UserEntity originalVoter, VotingEntity voting) {
        this.voter = voter;
        this.originalVoter = originalVoter;
        this.voting = voting;
    }

    public void addVoteDetails(VoteDetailsEntity detail) {
        if (voteDetails == null) {
            voteDetails = new ArrayList<>();
        }
        this.voteDetails.add(detail);
        detail.setVote(this);
    }
}
