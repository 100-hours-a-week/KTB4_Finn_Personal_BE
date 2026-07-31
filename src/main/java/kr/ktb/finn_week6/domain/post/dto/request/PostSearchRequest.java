package kr.ktb.finn_week6.domain.post.dto.request;

import jakarta.validation.constraints.NotBlank;
import kr.ktb.finn_week6.domain.post.dto.command.PostSearchCommand;
import kr.ktb.finn_week6.domain.post.dto.enums.DateFilterType;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record PostSearchRequest(
        @NotBlank(message = "tag is required")
        String tag,
        DateFilterType dateFilterType,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate targetDate,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate startDate,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate endDate
) {
        public PostSearchCommand toCommand(Long userId) {
                return new PostSearchCommand(tag(), dateFilterType(), targetDate(), startDate(), endDate(), userId);
        }
}
