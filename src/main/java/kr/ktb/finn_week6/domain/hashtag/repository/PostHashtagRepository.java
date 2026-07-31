package kr.ktb.finn_week6.domain.hashtag.repository;

import jakarta.persistence.EntityManager;
import kr.ktb.finn_week6.domain.hashtag.PostHashTag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PostHashtagRepository {
    private final EntityManager em;

    public PostHashTag save(PostHashTag postHashTag) {
        em.persist(postHashTag);
        return postHashTag;
    }

    public List<PostHashTag> findAllByPostId(Long postId) {
        return em.createQuery("select p from PostHashTag p where p.post.id = :postId", PostHashTag.class)
                .setParameter("postId", postId)
                .getResultList();
    }

    public void deleteAll(List<PostHashTag> postHashTags) {
        postHashTags.forEach(em::remove);
    }
}
