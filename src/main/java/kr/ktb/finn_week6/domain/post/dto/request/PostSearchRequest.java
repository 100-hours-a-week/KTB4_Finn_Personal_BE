package kr.ktb.finn_week6.domain.post.dto.request;

import jakarta.validation.constraints.NotBlank;
import kr.ktb.finn_week6.domain.post.dto.command.PostSearchCommand;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PostSearchRequest(
        @NotBlank(message = "tag is required")
        String tag,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate startDate,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate endDate
) {
        public PostSearchCommand toCommand(Long userId) {
                return new PostSearchCommand(tag(), startDate(), endDate(), userId);
        }
}
