package kr.ktb.finn_week6;


import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

@Configuration
@Profile("seed")
@RequiredArgsConstructor
public class SeedDataConfig {

    private static final int USER_COUNT = 100;
    private static final int HASHTAG_COUNT = 10;
    private static final int POST_COUNT = 200_000;
    private static final int TAGS_PER_POST = 3;
    private static final long RANDOM_SEED = 20260802L;

    private final JdbcTemplate jdbcTemplate;

    @Bean
    ApplicationRunner seedDataRunner() {
        return args -> {
            insertUsers();
            insertHashtags();
            insertPosts();
            insertPostHashtags();

            jdbcTemplate.execute(
                    "ANALYZE TABLE users, hash_tags, posts, post_hash_tags"
            );
        };
    }

    private void insertUsers() {
        List<Object[]> rows = IntStream.rangeClosed(1, USER_COUNT)
                .mapToObj(id -> new Object[]{
                        id,
                        "seed-user-" + id,
                        "seed-user-" + id + "@example.com",
                        "seed-password",
                        null,
                        Timestamp.valueOf("2025-08-02 00:00:00"),
                        false,
                        null
                })
                .toList();

        jdbcTemplate.batchUpdate("""
        INSERT INTO users (
            id, nickname, email, password, profile_img,
            created_at, is_deleted, deleted_at
        )
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """, rows);
    }

    private void insertHashtags() {
        List<Object[]> rows = new ArrayList<>();

        rows.add(new Object[]{1L, "popular_tag"});
        rows.add(new Object[]{2L, "normal_tag"});
        rows.add(new Object[]{3L, "rare_tag"});

        for (long id = 4; id <= HASHTAG_COUNT; id++) {
            rows.add(new Object[]{id, "tag_" + id});
        }

        jdbcTemplate.batchUpdate("""
        INSERT INTO hash_tags (id, tag_name)
        VALUES (?, ?)
        """, rows);
    }
    private void insertPosts() {
        Random random = new Random(RANDOM_SEED);
        LocalDateTime referenceTime =
                LocalDateTime.of(2026, 8, 2, 0, 0);

        List<Object[]> batch = new ArrayList<>();

        for (long postId = 1; postId <= POST_COUNT; postId++) {
            long userId = ((postId - 1) % USER_COUNT) + 1;

            long secondsWithinYear =
                    random.nextLong(365L * 24 * 60 * 60);

            LocalDateTime createdAt =
                    referenceTime.minusSeconds(secondsWithinYear);

            batch.add(new Object[]{
                    postId,
                    userId,
                    "seed-title-" + postId,
                    "seed-content-" + postId,
                    "https://example.com/seed-image.jpg",
                    0,
                    0,
                    0,
                    Timestamp.valueOf(createdAt),
                    null,
                    false,
                    null
            });

            if (batch.size() == 1_000) {
                insertPostBatch(batch);
                batch.clear();
            }
        }

        if (!batch.isEmpty()) {
            insertPostBatch(batch);
        }
    }

    private void insertPostBatch(List<Object[]> batch) {
        jdbcTemplate.batchUpdate("""
        INSERT INTO posts (
            id, user_id, title, content, content_img,
            view_count, like_count, comment_count,
            created_at, updated_at, is_deleted, deleted_at
        )
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """, batch);
    }
    private void insertPostHashtags() {
        List<Long> shuffledPostIds = LongStream
                .rangeClosed(1, POST_COUNT)
                .boxed()
                .collect(Collectors.toCollection(ArrayList::new));

        Collections.shuffle(
                shuffledPostIds,
                new Random(RANDOM_SEED + 1)
        );

        Map<Long, Long> specialTagByPostId = new HashMap<>();

        // 인기 태그: 200,000건
        for (int i = 0; i < 100_000; i++) {
            specialTagByPostId.put(shuffledPostIds.get(i), 1L);
        }

        // 중간 태그: 20,000건
        for (int i = 100_000; i < 120_000; i++) {
            specialTagByPostId.put(shuffledPostIds.get(i), 2L);
        }

        // 희소 태그: 1000건
        for (int i = 120_000; i < 122_000; i++) {
            specialTagByPostId.put(shuffledPostIds.get(i), 3L);
        }

        Random tagRandom = new Random(RANDOM_SEED + 2);
        List<Object[]> batch = new ArrayList<>();
        long relationId = 1;

        for (long postId = 1; postId <= POST_COUNT; postId++) {
            Set<Long> tagIds = new HashSet<>();

            Long specialTagId = specialTagByPostId.get(postId);

            if (specialTagId != null) {
                tagIds.add(specialTagId);
            }

            while (tagIds.size() < TAGS_PER_POST) {
                // 일반 태그는 4~1000
                long tagId =
                        4L + tagRandom.nextInt(HASHTAG_COUNT - 3);

                tagIds.add(tagId);
            }

            for (Long tagId : tagIds) {
                batch.add(new Object[]{
                        relationId++,
                        postId,
                        tagId
                });
            }

            if (batch.size() >= 5_000) {
                insertPostHashtagBatch(batch);
                batch.clear();
            }
        }

        if (!batch.isEmpty()) {
            insertPostHashtagBatch(batch);
        }
    }

    private void insertPostHashtagBatch(List<Object[]> batch) {
        jdbcTemplate.batchUpdate("""
        INSERT INTO post_hash_tags (
            id, post_id, hashtag_id
        )
        VALUES (?, ?, ?)
        """, batch);
    }
}

