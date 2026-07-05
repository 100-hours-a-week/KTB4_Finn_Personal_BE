package kr.ktb.finn_week6.domain.post.repository;

import jakarta.persistence.EntityManager;
import kr.ktb.finn_week6.domain.post.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PostRepository {
    private final EntityManager em;

    public Post save(Post post) {
        em.persist(post);
        return post;
    }

    public Optional<Post> findById(Long id){
        return Optional.ofNullable(em.find(Post.class, id));
    }
    public List<Post> findByUserId(Long userId){
        return em.createQuery("select p from Post p where p.user.id = :userId", Post.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    public List<Post> findPostsOrderByCreatedAtDesc(){
        return em.createQuery("select p from Post p join fetch p.user where p.isDeleted = false order by p.createdAt desc", Post.class)
                .setMaxResults(10)
                .getResultList();
    }
}
