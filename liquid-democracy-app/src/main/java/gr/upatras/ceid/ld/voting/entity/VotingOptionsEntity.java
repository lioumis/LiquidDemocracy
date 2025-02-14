package gr.upatras.ceid.ld.voting.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@Entity(name = "voting_option")
public class VotingOptionsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Setter
    @ManyToOne
    @JoinColumn(name = "voting_id")
    private VotingEntity voting;

    public VotingOptionsEntity(String name, String description, VotingEntity voting) {
        this.name = name;
        this.description = description;
        this.voting = voting;
    }
}
