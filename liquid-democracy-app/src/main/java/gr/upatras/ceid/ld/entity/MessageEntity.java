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

    @Column(nullable = false)
    private String content;

    //TODO: Like & Dislike. Probably new table needed to track which user did the action so that they are not able to do it again..
}