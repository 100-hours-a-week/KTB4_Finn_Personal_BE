package kr.ktb.finn_week6.domain.post.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import kr.ktb.finn_week6.domain.post.dto.command.UpdatePostCommand;

public record UpdatePostRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 26, message = "Title must be less than 26 characters")
        String title,
        @NotBlank(message = "Content is required")
        String content,
        String contentImg
) {
    public UpdatePostCommand toCommand(Long userId, Long postId){

        return new UpdatePostCommand(userId,postId,title(),content(),contentImg());
    }
}
