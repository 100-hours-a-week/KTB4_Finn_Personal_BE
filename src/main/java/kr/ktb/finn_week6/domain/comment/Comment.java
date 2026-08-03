package kr.ktb.finn_week6.domain.comment;

import jakarta.persistence.*;
import kr.ktb.finn_week6.domain.post.Post;
import kr.ktb.finn_week6.domain.user.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Getter
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isDeleted;
    private LocalDateTime deletedAt;

    public Comment(User user, Post post, String content) {
        this.user = user;
        this.post = post;
        this.content = content;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = null;
        this.isDeleted = false;
        this.deletedAt = null;
    }

    protected Comment() {
    }

    public void updateComment(String content){
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    public void setDeleted(){
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }
}
