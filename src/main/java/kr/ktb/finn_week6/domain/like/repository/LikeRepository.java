package kr.ktb.finn_week6.domain.like.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import kr.ktb.finn_week6.domain.like.Like;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static kr.ktb.finn_week6.domain.like.QLike.like;
import static kr.ktb.finn_week6.domain.post.QPost.post;

@Repository
@RequiredArgsConstructor
public class LikeRepository {
    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public void save(Like like){
        em.persist(like);
    }

    public Optional<Like> findById(Long id){
     return Optional.ofNullable(em.find(Like.class, id));
    }

    public List<Like> findUndeletedByPostIdsAndUserId(Set<Long> postIds, Long userId){
        return queryFactory
                .select(like)
                        .from(like)
                .join(like.post, post).fetchJoin()
                .where(
                        like.post.id.in(postIds),
                        like.user.id.eq(userId),
                        like.isDeleted.eq(false)
                ).fetch();
    }
    public Optional<Like> findByPostIdAndUserId(Long postId, Long userId){
        return em.createQuery("SELECT l FROM Like l WHERE l.post.id = :postId AND l.user.id = :userId", Like.class)
                .setParameter("postId", postId)
                .setParameter("userId", userId)
                .getResultList().stream().findFirst();
    }

    public List<Like> findUndeletedByPostId(Long postId){
        return em.createQuery("SELECT l FROM Like l join fetch l.post WHERE l.post.id = :postId AND l.isDeleted = false ", Like.class)
                .setParameter("postId", postId)
                .getResultList();
    }

    public Optional<Like> findUndeletedByPostIdAndUserId(Long postId, Long loginUserId) {
        return queryFactory
                .select(like)
                .from(like)
                .where(
                        like.post.id.eq(postId),
                        like.user.id.eq(loginUserId),
                        like.isDeleted.eq(false)
                ).stream().findFirst();
    }
}
