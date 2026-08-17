package kr.ktb.finn_week6.domain.post.repository;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import kr.ktb.finn_week6.domain.post.Post;
import kr.ktb.finn_week6.domain.post.dto.command.PostSearchCommand;
import kr.ktb.finn_week6.domain.post.dto.response.MostViewPostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static kr.ktb.finn_week6.domain.hashtag.QHashTag.hashTag;
import static kr.ktb.finn_week6.domain.hashtag.QPostHashTag.postHashTag;
import static kr.ktb.finn_week6.domain.place.QPlace.place;
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

    public List<Post> findPostsOrderByCreatedAtDesc(
            LocalDateTime cursorCreatedAt,
            Long cursorId,
            int limit
    ) {
        List<Long> postIds = queryFactory
                .select(post.id)
                .from(post)
                .where(
                        post.isDeleted.isFalse(),
                        recentCursorCondition(cursorCreatedAt, cursorId)
                )
                .orderBy(post.createdAt.desc(),
                        post.id.desc())
                .limit(limit)
                .fetch();

        if (postIds.isEmpty()) {
            return List.of();
        }

        Map<Long, Post> postsById = queryFactory
                .selectFrom(post)
                .join(post.user, user).fetchJoin()
                .leftJoin(post.place, place).fetchJoin()
                .where(post.id.in(postIds))
                .fetch()
                .stream()
                .collect(Collectors.toMap(Post::getId, Function.identity()));

        return postIds.stream()
                .map(postsById::get)
                .toList();
    }

    public List<Post> findPostsOrderByLikeCountDesc(
            Integer cursorLikeCount,
            LocalDateTime cursorCreatedAt,
            Long cursorId,
            int limit
    ) {
        List<Long> postIds = queryFactory
                .select(post.id)
                .from(post)
                .where(
                        post.isDeleted.isFalse(),
                        post.likeCount.gt(0),
                        popularCursorCondition(cursorLikeCount,cursorCreatedAt, cursorId)
                )
                .orderBy(
                        post.likeCount.desc(),
                        post.createdAt.desc(),
                        post.id.desc())
                .limit(limit)
                .fetch();

        if (postIds.isEmpty()) {
            return List.of();
        }

        Map<Long, Post> postsById = queryFactory
                .selectFrom(post)
                .join(post.user, user).fetchJoin()
                .leftJoin(post.place, place).fetchJoin()
                .where(post.id.in(postIds))
                .fetch()
                .stream()
                .collect(Collectors.toMap(Post::getId, Function.identity()));

        return postIds.stream()
                .map(postsById::get)
                .toList();
    }



    public List<Post> findPostsBySearchTag(PostSearchCommand command) {
        return queryFactory
                .select(post)
                .from(postHashTag)
                .join(postHashTag.post, post)
                .join(post.user, user).fetchJoin()
                .join(postHashTag.hashtag, hashTag)
                .leftJoin(post.place, place).fetchJoin()
                .where(

                        post.isDeleted.eq(false),
                        hashTag.tagName.eq(command.tag()),
                        dateRange(command)
                )
                .orderBy(
                        postHashTag.createdAt.desc(),
                        postHashTag.postId.desc()
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


    public List<Post> findPostsByUserId(Long userId) {
        return queryFactory
                .selectFrom(post)
                .join(post.user, user).fetchJoin()
                .leftJoin(post.place, place).fetchJoin()
                .where(post.isDeleted.isFalse())
                .where(post.user.id.eq(userId))
                .orderBy(post.createdAt.desc())
                .fetch();
    }

    private BooleanExpression dateRange(PostSearchCommand command) {
        LocalDateTime start = command.startDate().atStartOfDay();
        LocalDateTime end = command.endDate().plusDays(1).atStartOfDay();

        return postHashTag.createdAt.goe(start)
                .and(postHashTag.createdAt.lt(end));
    }

    private BooleanExpression recentCursorCondition(LocalDateTime cursorCreatedAt, Long cursorId) {
        if(cursorCreatedAt == null && cursorId == null){
            return null;
        }
        if(cursorCreatedAt == null || cursorId == null){
            throw new IllegalArgumentException("cursorCreatedAt과 cursorId 모두 필요합니다.");
        }

        return post.createdAt.lt(cursorCreatedAt)
                .or(
                        post.createdAt.eq(cursorCreatedAt)
                                .and(post.id.lt(cursorId))
                );
    }

    private BooleanExpression popularCursorCondition(
            Integer cursorLikeCount,
            LocalDateTime cursorCreatedAt,
            Long cursorId
    ) {
        if (cursorLikeCount == null && cursorCreatedAt == null && cursorId == null) {
            return null;
        }

        if (cursorLikeCount == null || cursorCreatedAt == null || cursorId == null) {
            throw new IllegalArgumentException(
                    "인기순 Cursor 값이 모두 필요합니다."
            );
        }

        return post.likeCount.lt(cursorLikeCount)
                .or(
                        post.likeCount.eq(cursorLikeCount)
                                .and(post.createdAt.lt(cursorCreatedAt))
                )
                .or(
                        post.likeCount.eq(cursorLikeCount)
                                .and(post.createdAt.eq(cursorCreatedAt))
                                .and(post.id.lt(cursorId))
                );
    }
}
