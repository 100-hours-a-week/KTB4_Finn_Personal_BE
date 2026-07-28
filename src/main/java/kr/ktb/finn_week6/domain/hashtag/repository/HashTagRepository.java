package kr.ktb.finn_week6.domain.hashtag.repository;


import jakarta.persistence.EntityManager;
import kr.ktb.finn_week6.domain.hashtag.HashTag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class HashTagRepository {
    private final EntityManager em;

    public void save(HashTag hashTag){
        em.persist(hashTag);
    }

    public Optional<HashTag> findById(Long id){
        return Optional.ofNullable(em.find(HashTag.class, id));
    }

    public Optional<HashTag> findByTagName(String tagName) {
        return em.createQuery(
                        "select h from HashTag h where h.tagName = :tagName",
                        HashTag.class
                )
                .setParameter("tagName", tagName)
                .getResultStream()
                .findFirst();
    }
}
