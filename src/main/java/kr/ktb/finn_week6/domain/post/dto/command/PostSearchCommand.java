package kr.ktb.finn_week6.domain.post.dto.command;

import kr.ktb.finn_week6.domain.post.dto.enums.DateFilterType;
import kr.ktb.finn_week6.domain.post.dto.request.PostSearchRequest;

import java.time.LocalDate;

public record PostSearchCommand(
        String tag,
        DateFilterType dateFilterType,

        LocalDate targetDate,
        LocalDate startDate,
        LocalDate endDate,

        Long userId
) {
}
