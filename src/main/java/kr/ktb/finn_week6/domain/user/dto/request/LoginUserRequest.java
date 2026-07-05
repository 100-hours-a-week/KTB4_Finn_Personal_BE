package kr.ktb.finn_week6.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import kr.ktb.finn_week6.domain.user.dto.command.LoginUserCommand;

public record LoginUserRequest(
        @NotBlank(message = "Email is required")
        @Email
        String email,

        //@Size(min = 8, message = "TOO_SHORT") // 길이가 8 미만
        //@Size(max = 20, message = "TOO_LONG") // 길이가 20 초과
//        @Pattern(
//                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$",
//                message = "INVALID_FORMAT"
//        ) // 영문, 숫자, 특수문자를 최소 1개씩 포함해야 함
        String password
) {
    public LoginUserCommand loginUserCommand(){
        return new LoginUserCommand(email(), password());
    }
}
