package gr.upatras.ceid.ld.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@Entity(name = "vote_details")
public class VoteDetailsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ManyToOne
    @JoinColumn(name = "vote_id")
    private VoteEntity vote;

    @Column(name = "vote_rank")
    private Integer rank;

    @ManyToOne
    @JoinColumn(name = "voting_option_id", nullable = false)
    private VotingOptionsEntity votingOption;

    public VoteDetailsEntity(Integer rank, VotingOptionsEntity votingOption) {
        this.rank = rank;
        this.votingOption = votingOption;
    }
}
