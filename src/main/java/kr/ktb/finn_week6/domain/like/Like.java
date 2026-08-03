package kr.ktb.finn_week6.domain.like;

import jakarta.persistence.*;
import kr.ktb.finn_week6.domain.post.Post;
import kr.ktb.finn_week6.domain.user.User;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "likes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_like_user_post",
                        columnNames = {"user_id", "post_id"}
                )
        }
        )
@Getter
public class Like {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    private LocalDateTime createdAt;
    boolean isDeleted;
    private LocalDateTime deletedAt;

    protected Like() {
    }
    public Like(User user, Post post) {
        this.user = user;
        this.post = post;
        this.createdAt = LocalDateTime.now();
        this.isDeleted = false;
        this.deletedAt = null;
    }

    public void setDeleted(){
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }
    public void recovered(){
        this.isDeleted = false;
        this.deletedAt = null;
    }
}
