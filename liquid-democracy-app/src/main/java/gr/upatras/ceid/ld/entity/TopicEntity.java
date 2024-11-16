package gr.upatras.ceid.ld.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Entity(name = "topic")
@NoArgsConstructor
public class TopicEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    @OneToMany(mappedBy = "topic")
    private List<VoteEntity> votes;

    @OneToMany(mappedBy = "topic")
    private List<DelegationEntity> delegations;

    private LocalDateTime deadline;

    public TopicEntity(Long id) {
        this.id = id;
    }
}