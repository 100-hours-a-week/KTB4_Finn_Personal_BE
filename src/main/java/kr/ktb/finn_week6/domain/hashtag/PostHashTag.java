package kr.ktb.finn_week6.domain.hashtag;

import jakarta.persistence.*;
import kr.ktb.finn_week6.domain.post.Post;
import lombok.Getter;

@Entity
@Table(name = "PostHashTags")
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
}
