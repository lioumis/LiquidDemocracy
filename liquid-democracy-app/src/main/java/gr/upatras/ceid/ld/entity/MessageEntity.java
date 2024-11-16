package gr.upatras.ceid.ld.entity;

import jakarta.persistence.*;

@Entity(name = "message")
public class MessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "discussion_id")
    private DiscussionEntity discussion;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    private String content;
}