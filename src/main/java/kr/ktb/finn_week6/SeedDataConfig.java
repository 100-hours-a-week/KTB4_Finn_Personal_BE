package kr.ktb.finn_week6;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.IntStream;

@Configuration
@Profile("seed")
@RequiredArgsConstructor
public class SeedDataConfig {

    private static final Logger log = LoggerFactory.getLogger(SeedDataConfig.class);

    private static final int USER_COUNT = 100;
    private static final int HASHTAG_COUNT = 10;
    private static final int DAYS_TO_SEED = 365;
    private static final int POSTS_PER_DAY = 10_000;
    private static final int POST_COUNT = DAYS_TO_SEED * POSTS_PER_DAY;
    private static final int TAGS_PER_POST = 3;
    private static final int POPULAR_TAGS_PER_DAY = 4_000;
    private static final int NORMAL_TAGS_PER_DAY = 1_000;
    private static final int RARE_TAGS_PER_DAY = 100;
    private static final int BATCH_SIZE = 1_000;
    private static final long RANDOM_SEED = 20260802L;
    private static final ZoneId SEED_ZONE = ZoneId.of("Asia/Seoul");

    private final JdbcTemplate jdbcTemplate;

    @Value("${seed.reset-enabled:false}")
    private boolean resetEnabled;

    @Value("${seed.reference-date:}")
    private String configuredReferenceDate;

    @Bean
    ApplicationRunner seedDataRunner() {
        return args -> {
            if (!resetEnabled) {
                log.warn("Seed data generation skipped. Set SEED_RESET_ENABLED=true to reset and seed the database.");
                return;
            }

            LocalDate referenceDate = resolveReferenceDate(configuredReferenceDate);

            resetSeedData();
            insertUsers(referenceDate);
            insertHashtags();
            insertPosts(referenceDate);
            insertPostHashtags(referenceDate);

            jdbcTemplate.execute("ANALYZE TABLE users, hash_tags, posts, post_hash_tags");

            log.info(
                    "Seed data generation completed: referenceDate={}, days={}, postsPerDay={}, totalPosts={}",
                    referenceDate,
                    DAYS_TO_SEED,
                    POSTS_PER_DAY,
                    POST_COUNT
            );
        };
    }

    LocalDate resolveReferenceDate(String referenceDate) {
        if (referenceDate == null || referenceDate.isBlank()) {
            return LocalDate.now(SEED_ZONE);
        }

        try {
            return LocalDate.parse(referenceDate);
        } catch (DateTimeException exception) {
            throw new IllegalArgumentException(
                    "SEED_REFERENCE_DATE must use ISO-8601 format (yyyy-MM-dd): " + referenceDate,
                    exception
            );
        }
    }

    private void resetSeedData() {
        jdbcTemplate.update("DELETE FROM comments");
        jdbcTemplate.update("DELETE FROM likes");
        jdbcTemplate.update("DELETE FROM post_hash_tags");
        jdbcTemplate.update("DELETE FROM posts");
        jdbcTemplate.update("DELETE FROM hash_tags");
        jdbcTemplate.update("DELETE FROM users");
    }

    private void insertUsers(LocalDate referenceDate) {
        Timestamp createdAt = Timestamp.valueOf(referenceDate.minusDays(DAYS_TO_SEED - 1).atStartOfDay());

        List<Object[]> rows = IntStream.rangeClosed(1, USER_COUNT)
                .mapToObj(id -> new Object[]{
                        id,
                        "seed-user-" + id,
                        "seed-user-" + id + "@example.com",
                        "seed-password",
                        null,
                        createdAt,
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

    private void insertPosts(LocalDate referenceDate) {
        LocalDate firstDate = referenceDate.minusDays(DAYS_TO_SEED - 1L);

        for (int dayIndex = 0; dayIndex < DAYS_TO_SEED; dayIndex++) {
            List<Object[]> dailyRows = createPostRowsForDay(firstDate.plusDays(dayIndex), dayIndex);
            insertPostBatch(dailyRows);
        }
    }

    List<Object[]> createPostRowsForDay(LocalDate postDate, int dayIndex) {
        List<Timestamp> createdAtByPostIndex = createPostTimestampsForDay(postDate, dayIndex);
        List<Object[]> rows = new ArrayList<>(POSTS_PER_DAY);
        long firstPostId = (long) dayIndex * POSTS_PER_DAY + 1;

        for (int postIndex = 0; postIndex < POSTS_PER_DAY; postIndex++) {
            long postId = firstPostId + postIndex;
            long userId = ((postId - 1) % USER_COUNT) + 1;

            rows.add(new Object[]{
                    postId,
                    userId,
                    "seed-title-" + postId,
                    "seed-content-" + postId,
                    "https://example.com/seed-image.jpg",
                    0,
                    0,
                    0,
                    createdAtByPostIndex.get(postIndex),
                    null,
                    false,
                    null
            });
        }

        return rows;
    }

    List<Timestamp> createPostTimestampsForDay(LocalDate postDate, int dayIndex) {
        Random timeRandom = new Random(RANDOM_SEED + dayIndex);
        List<Timestamp> timestamps = new ArrayList<>(POSTS_PER_DAY);

        for (int postIndex = 0; postIndex < POSTS_PER_DAY; postIndex++) {
            int secondOfDay = timeRandom.nextInt(24 * 60 * 60);
            LocalDateTime createdAt = postDate.atStartOfDay().plusSeconds(secondOfDay);
            timestamps.add(Timestamp.valueOf(createdAt));
        }

        return timestamps;
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

    private void insertPostHashtags(LocalDate referenceDate) {
        List<Object[]> batch = new ArrayList<>(BATCH_SIZE);
        long relationId = 1L;
        LocalDate firstDate = referenceDate.minusDays(DAYS_TO_SEED - 1L);

        for (int dayIndex = 0; dayIndex < DAYS_TO_SEED; dayIndex++) {
            Map<Long, Set<Long>> dailyTagsByPostId = createTagAssignmentsForDay(dayIndex);
            List<Timestamp> createdAtByPostIndex =
                    createPostTimestampsForDay(firstDate.plusDays(dayIndex), dayIndex);
            long firstPostId = (long) dayIndex * POSTS_PER_DAY + 1;

            for (Map.Entry<Long, Set<Long>> entry : dailyTagsByPostId.entrySet()) {
                int postIndex = Math.toIntExact(entry.getKey() - firstPostId);
                Timestamp createdAt = createdAtByPostIndex.get(postIndex);

                for (Long tagId : entry.getValue()) {
                    batch.add(new Object[]{relationId++, entry.getKey(), tagId, createdAt});

                    if (batch.size() == BATCH_SIZE) {
                        insertPostHashtagBatch(batch);
                        batch.clear();
                    }
                }
            }
        }

        if (!batch.isEmpty()) {
            insertPostHashtagBatch(batch);
        }
    }

    Map<Long, Set<Long>> createTagAssignmentsForDay(int dayIndex) {
        Map<Long, Set<Long>> tagsByPostId = new TreeMap<>();
        long firstPostId = (long) dayIndex * POSTS_PER_DAY + 1;

        for (int postIndex = 0; postIndex < POSTS_PER_DAY; postIndex++) {
            tagsByPostId.put(firstPostId + postIndex, new TreeSet<>());
        }

        assignSpecialTag(tagsByPostId, firstPostId, dayIndex, 1L, POPULAR_TAGS_PER_DAY);
        assignSpecialTag(tagsByPostId, firstPostId, dayIndex, 2L, NORMAL_TAGS_PER_DAY);
        assignSpecialTag(tagsByPostId, firstPostId, dayIndex, 3L, RARE_TAGS_PER_DAY);

        Random fillerTagRandom = new Random(RANDOM_SEED + 10_000 + dayIndex);
        for (Set<Long> tagIds : tagsByPostId.values()) {
            while (tagIds.size() < TAGS_PER_POST) {
                long tagId = 4L + fillerTagRandom.nextInt(HASHTAG_COUNT - 3);
                tagIds.add(tagId);
            }
        }

        return tagsByPostId;
    }

    int totalPostCount() {
        return POST_COUNT;
    }

    private void assignSpecialTag(
            Map<Long, Set<Long>> tagsByPostId,
            long firstPostId,
            int dayIndex,
            long tagId,
            int count
    ) {
        List<Long> dailyPostIds = new ArrayList<>(POSTS_PER_DAY);
        for (int postIndex = 0; postIndex < POSTS_PER_DAY; postIndex++) {
            dailyPostIds.add(firstPostId + postIndex);
        }

        long shuffleSeed = RANDOM_SEED + (dayIndex * 31L) + (tagId * 1_000_003L);
        Collections.shuffle(dailyPostIds, new Random(shuffleSeed));

        for (int index = 0; index < count; index++) {
            tagsByPostId.get(dailyPostIds.get(index)).add(tagId);
        }
    }

    private void insertPostHashtagBatch(List<Object[]> batch) {
        jdbcTemplate.batchUpdate("""
                INSERT INTO post_hash_tags (
                    id, post_id, hashtag_id, created_at
                )
                VALUES (?, ?, ?, ?)
                """, batch);
    }
}
