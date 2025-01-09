package gr.upatras.ceid.ld.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity(name = "feedback")
public class FeedbackEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "voting_id")
    private VotingEntity voting;

    @Column(nullable = false)
    private String content;

    public FeedbackEntity(UserEntity user, String content, VotingEntity voting) {
        this.user = user;
        this.content = content;
        this.voting = voting;
    }
}