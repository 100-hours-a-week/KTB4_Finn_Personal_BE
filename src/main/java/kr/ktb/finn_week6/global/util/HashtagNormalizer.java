package kr.ktb.finn_week6.global.util;

import java.util.List;
import java.util.Objects;

public class HashtagNormalizer {
    public static List<String> normalize(List<String> tags) {
        if (tags == null) {
            return null;
        }

        return tags.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(tag -> !tag.isBlank())
                .distinct()
                .toList();
    }
}
