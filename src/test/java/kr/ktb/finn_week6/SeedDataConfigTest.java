package kr.ktb.finn_week6;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class SeedDataConfigTest {

    private final SeedDataConfig seedDataConfig = new SeedDataConfig(mock(JdbcTemplate.class));

    @Test
    void createsTenThousandPostsForEachDay() {
        LocalDate referenceDate = LocalDate.of(2026, 8, 4);

        List<Object[]> oldestRows = seedDataConfig.createPostRowsForDay(referenceDate.minusDays(364), 0);
        List<Object[]> referenceDateRows = seedDataConfig.createPostRowsForDay(referenceDate, 364);

        assertThat(seedDataConfig.totalPostCount()).isEqualTo(3_650_000);
        assertThat(oldestRows).hasSize(10_000);
        assertThat(referenceDateRows).hasSize(10_000);
        assertThat(oldestRows).allMatch(row -> postDate(row).equals(referenceDate.minusDays(364)));
        assertThat(referenceDateRows).allMatch(row -> postDate(row).equals(referenceDate));
        assertThat(oldestRows.getFirst()[0]).isEqualTo(1L);
        assertThat(referenceDateRows.getLast()[0]).isEqualTo(3_650_000L);
    }

    @Test
    void assignsExactDailyTagDistributionAndThreeTagsPerPost() {
        for (int dayIndex : List.of(0, 123, 364)) {
            Map<Long, Set<Long>> tagsByPostId = seedDataConfig.createTagAssignmentsForDay(dayIndex);
            List<Set<Long>> dailyTags = tagsByPostId.values().stream().toList();

            assertThat(tagsByPostId).hasSize(10_000);
            assertThat(tagsByPostId.values()).allMatch(tags -> tags.size() == 3);
            assertThat(countContaining(dailyTags, 1L)).isEqualTo(4_000);
            assertThat(countContaining(dailyTags, 2L)).isEqualTo(1_000);
            assertThat(countContaining(dailyTags, 3L)).isEqualTo(100);
            assertThat(countContaining(dailyTags, 11L)).isZero();
        }
    }

    @Test
    void providesExpectedTagCountsForEverySearchWindow() {
        for (int days : List.of(1, 7, 30, 90, 365)) {
            int popularCount = 0;
            int normalCount = 0;
            int rareCount = 0;
            int missingCount = 0;

            for (int dayIndex = 365 - days; dayIndex < 365; dayIndex++) {
                List<Set<Long>> dailyTags = seedDataConfig.createTagAssignmentsForDay(dayIndex)
                        .values()
                        .stream()
                        .toList();

                popularCount += countContaining(dailyTags, 1L);
                normalCount += countContaining(dailyTags, 2L);
                rareCount += countContaining(dailyTags, 3L);
                missingCount += countContaining(dailyTags, 11L);
            }

            assertThat(popularCount).isEqualTo(4_000 * days);
            assertThat(normalCount).isEqualTo(1_000 * days);
            assertThat(rareCount).isEqualTo(100 * days);
            assertThat(missingCount).isZero();
        }
    }

    @Test
    void producesDeterministicPostsAndTagAssignments() {
        LocalDate referenceDate = LocalDate.of(2026, 8, 4);

        List<Object[]> firstPosts = seedDataConfig.createPostRowsForDay(referenceDate, 364);
        List<Object[]> secondPosts = seedDataConfig.createPostRowsForDay(referenceDate, 364);

        assertThat(secondPosts).usingRecursiveFieldByFieldElementComparator().isEqualTo(firstPosts);
        assertThat(seedDataConfig.createTagAssignmentsForDay(364))
                .isEqualTo(seedDataConfig.createTagAssignmentsForDay(364));
    }

    @Test
    void reusesPostCreationTimestampsForHashtagRows() {
        LocalDate postDate = LocalDate.of(2026, 8, 4);

        List<Object[]> postRows = seedDataConfig.createPostRowsForDay(postDate, 364);
        List<Timestamp> timestamps = seedDataConfig.createPostTimestampsForDay(postDate, 364);

        assertThat(timestamps).hasSize(postRows.size());
        for (int postIndex : List.of(0, 4_999, 9_999)) {
            assertThat(timestamps.get(postIndex)).isEqualTo(postRows.get(postIndex)[8]);
        }
    }

    @Test
    void resolvesConfiguredReferenceDateAndRejectsInvalidFormat() {
        assertThat(seedDataConfig.resolveReferenceDate("2026-08-04"))
                .isEqualTo(LocalDate.of(2026, 8, 4));
        assertThrows(
                IllegalArgumentException.class,
                () -> seedDataConfig.resolveReferenceDate("2026/08/04")
        );
    }

    private int countContaining(List<Set<Long>> dailyTags, long tagId) {
        Predicate<Set<Long>> containsTag = tags -> tags.contains(tagId);
        return (int) dailyTags.stream().filter(containsTag).count();
    }

    private LocalDate postDate(Object[] row) {
        return ((Timestamp) row[8]).toLocalDateTime().toLocalDate();
    }

}
