package kr.ktb.finn_week6.domain.post.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import kr.ktb.finn_week6.domain.post.Post;
import kr.ktb.finn_week6.domain.post.dto.response.MostViewPostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static kr.ktb.finn_week6.domain.post.QPost.post;
import static kr.ktb.finn_week6.domain.user.QUser.user;


@Repository
@RequiredArgsConstructor
public class PostRepository {
    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

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

    public List<MostViewPostResponse> findPostsOrderByViewCountDesc() {
        return queryFactory
                .select(Projections.constructor(
                        MostViewPostResponse.class,
                        post.title,
                        post.user.nickname
                ))
                .from(post)
                .join(post.user, user)
                .where(post.isDeleted.isFalse())
                .orderBy(post.viewCount.desc())
                .limit(3)
                .fetch();
    }

    public List<Post> findPostsOrderByLikeCountDesc() {
        return queryFactory
                .selectFrom(post)
                .join(post.user, user).fetchJoin()
                .where(post.isDeleted.isFalse())
                .where(post.likeCount.gt(0))
                .orderBy(post.likeCount.desc())
                .limit(10)
                .fetch();
    }

    public List<Post> findPostsByUserId(Long userId) {
        return queryFactory
                .selectFrom(post)
                .join(post.user, user).fetchJoin()
                .where(post.isDeleted.isFalse())
                .where(post.user.id.eq(userId))
                .orderBy(post.createdAt.desc())
                .fetch();
    }
}
