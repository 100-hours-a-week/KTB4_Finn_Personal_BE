package kr.ktb.finn_week6.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import kr.ktb.finn_week6.domain.user.dto.command.CreateUserCommand;

public record CreateUserRequest(
        @NotBlank(message = "Nickname is required")
        @Size(max = 10, message = "Nickname must be less than 10 characters")
        String nickname,

        @NotBlank(message = "Email is required")
        @Email
        String email,

        @NotBlank(message = "Password is required")
        String password,

        String profileImg
) {
    public CreateUserCommand createUserCommand(){
        return new CreateUserCommand(nickname(), email(), password(), profileImg());
    }
}
