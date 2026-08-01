package kr.ktb.finn_week6.domain.post.dto.command;

import java.time.LocalDate;
public record PostSearchCommand(
        String tag,
        LocalDate startDate,
        LocalDate endDate,

        Long userId
) {
}
