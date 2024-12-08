package gr.upatras.ceid.ld.entity;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Entity(name = "message")
public class MessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "voting_id")
    private VotingEntity voting;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(nullable = false)
    private String content;

    public MessageEntity(UserEntity user, String content, VotingEntity voting) {
        this.user = user;
        this.content = content;
        this.voting = voting;
    }

    //TODO: Like & Dislike. Probably new table needed to track which user did the action so that they are not able to do it again..
}