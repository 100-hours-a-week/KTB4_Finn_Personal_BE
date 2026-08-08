package kr.ktb.finn_week6.domain.hashtag.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import kr.ktb.finn_week6.domain.hashtag.PostHashTag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

import static kr.ktb.finn_week6.domain.hashtag.QHashTag.hashTag;
import static kr.ktb.finn_week6.domain.hashtag.QPostHashTag.postHashTag;

@Repository
@RequiredArgsConstructor
public class PostHashtagRepository {
    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public PostHashTag save(PostHashTag postHashTag) {
        em.persist(postHashTag);
        return postHashTag;
    }

    public List<PostHashTag> findAllByPostIds(Set<Long> postIds) {
        return queryFactory
                .select(postHashTag)
                .from(postHashTag)
                .join(postHashTag.hashtag, hashTag).fetchJoin()
                .where(
                        postHashTag.post.id.in(postIds)
                ).fetch();
    }

    public List<PostHashTag> findTagsByPostId(Long postId){
        return queryFactory
                .select(postHashTag)
                .from(postHashTag)
                .join(postHashTag.hashtag, hashTag).fetchJoin()
                .where(
                        postHashTag.post.id.eq(postId)
                ).fetch();
    }

    public void deleteAll(List<PostHashTag> postHashTags) {
        postHashTags.forEach(em::remove);
    }
}
