package kr.ktb.finn_week6.domain.post.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import kr.ktb.finn_week6.domain.post.Post;
import kr.ktb.finn_week6.domain.post.dto.command.PostSearchCommand;
import kr.ktb.finn_week6.domain.post.dto.response.MostViewPostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static kr.ktb.finn_week6.domain.hashtag.QHashTag.hashTag;
import static kr.ktb.finn_week6.domain.hashtag.QPostHashTag.postHashTag;
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

    public List<Post> findPostsBySearchTag(PostSearchCommand command) {
        return queryFactory
                .select(post)
                .from(postHashTag)
                .join(postHashTag.post, post)
                .join(post.user, user).fetchJoin()
                .join(postHashTag.hashtag, hashTag)
                .where(
                        post.isDeleted.eq(false),
                        hashTag.tagName.eq(command.tag()),
                        dateCondition(command)
                )
                .orderBy(
                        post.createdAt.desc(),
                        post.id.desc()
                )
                .limit(10)
                .fetch();
    }

    public List<MostViewPostResponse> findPostsOrderByViewCountDesc() {
        return queryFactory
                .select(Projections.constructor(
                        MostViewPostResponse.class,
                        post.id,
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

    private BooleanExpression dateCondition(PostSearchCommand command){
        if(command.dateFilterType() == null){
            return null;
        }

        LocalDate today = LocalDate.now();

        return switch (command.dateFilterType()){
            case TODAY -> dateRange(today, today);
            case LAST_WEEK ->  dateRange(today.minusDays(6), today);
            case SPECIFIC_DATE -> dateRange(command.targetDate(), command.targetDate());
            case CUSTOM_RANGE ->  dateRange(command.startDate(), command.endDate());
        };
    }

    private BooleanExpression dateRange(LocalDate startDate, LocalDate endDate){
        if(startDate == null || endDate == null){
            return null;
        }

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        return post.createdAt.goe(start).and(post.createdAt.lt(end));
    }
}
