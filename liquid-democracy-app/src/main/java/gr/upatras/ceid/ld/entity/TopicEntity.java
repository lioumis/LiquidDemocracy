package gr.upatras.ceid.ld.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Entity(name = "topic")
@NoArgsConstructor
public class TopicEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    //TODO: Is it needed?
    private String description;

    @OneToMany(mappedBy = "topic")
    private List<DelegationEntity> delegations;

    @OneToMany(mappedBy = "topic")
    private List<VotingEntity> votings;

    public TopicEntity(Long id) {
        this.id = id;
    }
}