package kr.ktb.finn_week6.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import kr.ktb.finn_week6.domain.user.dto.command.UpdatePasswordCommand;

public record UpdatePasswordRequest(
        @NotBlank(message = "current password is required")
        String currentPassword,
        @NotBlank(message = "new password is required")
        String newPassword
) {
    public UpdatePasswordCommand toCommand(Long sessionUserId){
        return new UpdatePasswordCommand(sessionUserId,currentPassword(), newPassword());
    }

}
