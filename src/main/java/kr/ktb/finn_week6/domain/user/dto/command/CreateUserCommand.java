package kr.ktb.finn_week6.domain.user.dto.command;
public record CreateUserCommand(
        String nickname,
        String email,
        String password,
        String profileImg
) {
}
