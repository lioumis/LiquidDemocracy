package gr.upatras.ceid.ld.entity;

import gr.upatras.ceid.ld.converter.VotingTypeConverter;
import gr.upatras.ceid.ld.enums.VotingType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@NoArgsConstructor
@Entity(name = "voting")
public class VotingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String information;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @OneToMany(mappedBy = "voting")
    private List<VoteEntity> votes;

    @Convert(converter = VotingTypeConverter.class)
    private VotingType votingType;

    @OneToMany(mappedBy = "voting")
    private List<VotingOptionsEntity> votingOptions;

    @ManyToOne
    @JoinColumn(name = "topic_id")
    private TopicEntity topic;

    @OneToMany(mappedBy = "voting", cascade = CascadeType.ALL)
    private List<MessageEntity> messages;

    @ManyToMany
    @JoinTable(
            name = "voting_committee",
            joinColumns = @JoinColumn(name = "voting_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<UserEntity> electoralCommittee = new HashSet<>();
}
