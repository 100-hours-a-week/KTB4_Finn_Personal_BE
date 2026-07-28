package kr.ktb.finn_week6.domain.hashtag;

import jakarta.persistence.*;
import kr.ktb.finn_week6.domain.post.Post;
import lombok.Getter;

@Entity
@Table(
        name = "PostHashTags",
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
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hashtag_id")
    private HashTag hashtag;

    public PostHashTag() {
    }

    public PostHashTag(Post post, HashTag hashtag) {
        this.post = post;
        this.hashtag = hashtag;
    }
}
