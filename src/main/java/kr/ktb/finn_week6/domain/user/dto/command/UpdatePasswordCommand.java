package kr.ktb.finn_week6.domain.user.dto.command;

public record UpdatePasswordCommand(
        Long loginUserId,
        String currentPassword,
        String newPassword
) {
}
