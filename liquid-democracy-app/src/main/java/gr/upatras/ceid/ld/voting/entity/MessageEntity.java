package gr.upatras.ceid.ld.voting.entity;

import gr.upatras.ceid.ld.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
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

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MessageDetailsEntity> messageDetails;

    public MessageEntity(UserEntity user, String content, VotingEntity voting) {
        this.user = user;
        this.content = content;
        this.voting = voting;
    }

    public void addMessageDetail(MessageDetailsEntity detail) {
        this.messageDetails.add(detail);
        detail.setMessage(this);
    }

    public void removeMessageDetail(MessageDetailsEntity detail) {
        this.messageDetails.remove(detail);
        detail.setMessage(null);
    }
}