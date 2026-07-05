package kr.ktb.finn_week6.domain.like.repository;

import jakarta.persistence.EntityManager;
import kr.ktb.finn_week6.domain.like.Like;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class LikeRepository {
    private final EntityManager em;

    public void save(Like like){
        em.persist(like);
    }

    public Optional<Like> findById(Long id){
     return Optional.ofNullable(em.find(Like.class, id));
    }

    public Optional<Like> findUndeletedByPostIdAndUserId(Long postId, Long userId){
        return em.createQuery("SELECT l FROM Like l WHERE l.post.id = :postId AND l.user.id = :userId AND l.isDeleted = false", Like.class)
                .setParameter("postId", postId)
                .setParameter("userId", userId)
                .getResultList().stream().findFirst();
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

}
