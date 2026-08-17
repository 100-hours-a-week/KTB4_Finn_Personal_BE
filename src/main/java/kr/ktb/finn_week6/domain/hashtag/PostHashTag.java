package kr.ktb.finn_week6.domain.hashtag;

import jakarta.persistence.*;
import kr.ktb.finn_week6.domain.post.Post;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "post_hash_tags",
        indexes = {
                @Index(
                        name = "idx_pht_hashtag_created_post",
                        columnList = "hashtag_id, created_at, post_id"
                )
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_post_hashtag",
                        columnNames = {"post_id", "hashtag_id"}
                )
        }
)
@Getter
public class PostHashTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hashtag_id", nullable = false)
    private HashTag hashtag;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;


    @Column(name = "post_id", insertable = false, updatable = false)
    private Long postId;


    public PostHashTag() {
    }

    public PostHashTag(Post post, HashTag hashtag) {
        this.post = post;
        this.hashtag = hashtag;
        this.createdAt = post.getCreatedAt();
    }
}
