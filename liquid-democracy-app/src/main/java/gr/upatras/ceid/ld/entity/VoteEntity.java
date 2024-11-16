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
    @JoinColumn(name = "topic_id")
    private TopicEntity topic;

    private String choice; //TODO: Enum

    public VoteEntity(UserEntity voter, TopicEntity topic, String choice) {
        this.voter = voter;
        this.topic = topic;
        this.choice = choice;
    }
}
