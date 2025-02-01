package gr.upatras.ceid.ld.entity;

import gr.upatras.ceid.ld.converter.VotingTypeConverter;
import gr.upatras.ceid.ld.enums.VotingType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @Setter
    @Column(nullable = false)
    private String information;

    @Setter
    @Column(nullable = false)
    private LocalDateTime startDate;

    @Setter
    @Column(nullable = false)
    private LocalDateTime endDate;

    @OneToMany(mappedBy = "voting")
    private List<VoteEntity> votes;

    @Setter
    @Convert(converter = VotingTypeConverter.class)
    private VotingType votingType;

    @Setter
    @Column
    private Integer voteLimit;

    @OneToMany(mappedBy = "voting", cascade = CascadeType.PERSIST, orphanRemoval = true)
    private List<VotingOptionsEntity> votingOptions;

    @OneToMany(mappedBy = "voting")
    private List<DelegationEntity> delegations;

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

    public VotingEntity(String name, TopicEntity topic, Set<UserEntity> electoralCommittee) {
        this.name = name;
        this.topic = topic;
        this.electoralCommittee = electoralCommittee;
    }

    public void addVotingOption(String name, String description) {
        if (this.votingOptions == null) {
            this.votingOptions = new ArrayList<>();
        }
        this.votingOptions.add(new VotingOptionsEntity(name, description, this));
    }

    public void clearVotingOptions() {
        this.votingOptions = null;
    }

    public void addMessage(String message, UserEntity user) {
        if (this.messages == null) {
            this.messages = new ArrayList<>();
        }
        this.messages.add(new MessageEntity(user, message, this));
    }
}
