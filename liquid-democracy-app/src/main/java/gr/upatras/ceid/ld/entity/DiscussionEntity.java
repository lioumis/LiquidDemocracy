package gr.upatras.ceid.ld.entity;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Entity(name = "discussion")
public class DiscussionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "topic_id")
    private TopicEntity topic;

    @OneToMany(mappedBy = "discussion", cascade = CascadeType.ALL)
    private List<MessageEntity> messages;

    private String summary;
}
