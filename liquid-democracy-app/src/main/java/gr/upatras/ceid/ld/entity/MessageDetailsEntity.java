package gr.upatras.ceid.ld.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@Entity(name = "message_details")
public class MessageDetailsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ManyToOne
    @JoinColumn(name = "message_id", nullable = false)
    private MessageEntity message;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Setter
    @Column(nullable = false)
    private boolean liked;

    public MessageDetailsEntity(MessageEntity message, UserEntity user, boolean liked) {
        this.message = message;
        this.user = user;
        this.liked = liked;
    }
}
