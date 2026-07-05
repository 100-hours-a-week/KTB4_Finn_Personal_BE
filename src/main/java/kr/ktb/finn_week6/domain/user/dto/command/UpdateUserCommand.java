package kr.ktb.finn_week6.domain.user.dto.command;

public record UpdateUserCommand(
        Long loginUserId,
        String nickname,
        String profileImg
) {
}
