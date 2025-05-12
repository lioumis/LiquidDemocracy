package gr.upatras.ceid.ld.voting.entity;

import gr.upatras.ceid.ld.common.converter.VotingTypeConverter;
import gr.upatras.ceid.ld.common.enums.VotingType;
import gr.upatras.ceid.ld.delegation.entity.DelegationEntity;
import gr.upatras.ceid.ld.topic.entity.TopicEntity;
import gr.upatras.ceid.ld.user.entity.UserEntity;
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
    @Column(length = 500)
    private String information;

    @Setter
    @Column
    private LocalDateTime startDate;

    @Setter
    @Column
    private LocalDateTime endDate;

    @OneToMany(mappedBy = "voting")
    private List<VoteEntity> votes;

    @Setter
    @Convert(converter = VotingTypeConverter.class)
    private VotingType votingType;

    @Setter
    @Column
    private Integer voteLimit;

    @Setter
    @Column(nullable = false)
    private boolean valid;

    @OneToMany(mappedBy = "voting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VotingOptionsEntity> votingOptions;

    @OneToMany(mappedBy = "voting")
    private List<DelegationEntity> delegations;

    @ManyToOne
    @JoinColumn(name = "topic_id", nullable = false)
    private TopicEntity topic;

    @OneToMany(mappedBy = "voting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MessageEntity> messages;

    @ManyToMany
    @JoinTable(
            name = "voting_committee",
            joinColumns = @JoinColumn(name = "voting_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<UserEntity> electoralCommittee = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "delegate",
            joinColumns = @JoinColumn(name = "voting_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<UserEntity> delegates = new HashSet<>();

    public VotingEntity(String name, TopicEntity topic, Set<UserEntity> electoralCommittee) {
        this.name = name;
        this.topic = topic;
        this.electoralCommittee = electoralCommittee;
        this.valid = true;
    }

    public void addVotingOption(String name, String description) {
        if (this.votingOptions == null) {
            this.votingOptions = new ArrayList<>();
        }
        this.votingOptions.add(new VotingOptionsEntity(name, description, this));
    }

    public void clearVotingOptions() {
        if (this.votingOptions != null) {
            this.votingOptions.clear();
        }
    }

    public void addMessage(String message, UserEntity user) {
        if (this.messages == null) {
            this.messages = new ArrayList<>();
        }
        this.messages.add(new MessageEntity(user, message, this));
    }
}
