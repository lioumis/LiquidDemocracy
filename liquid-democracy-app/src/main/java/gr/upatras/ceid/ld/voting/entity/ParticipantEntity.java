package gr.upatras.ceid.ld.voting.entity;

import gr.upatras.ceid.ld.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@Entity(name = "participant")
public class ParticipantEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "voting_id")
    private VotingEntity voting;

    @Setter
    private Boolean status;

    public ParticipantEntity(UserEntity user, VotingEntity voting) {
        this.user = user;
        this.voting = voting;
    }

    public ParticipantEntity(UserEntity user, VotingEntity voting, Boolean status) {
        this.user = user;
        this.voting = voting;
        this.status = status;
    }
}
