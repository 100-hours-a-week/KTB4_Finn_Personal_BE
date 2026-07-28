package kr.ktb.finn_week6.domain.post.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import kr.ktb.finn_week6.domain.post.dto.command.CreatePostCommand;
import org.hibernate.validator.constraints.URL;

import java.util.List;

public record CreatePostRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 26, message = "Title must be less than 26 characters")
        String title,
        @NotBlank(message = "Content is required")
        String content,
        @NotBlank(message = "ContentImg is required")
        @URL(message = "Content image must be a valid URL")
        String contentImg,

        @Size(max = 20, message = "Tag must be less than 26 characters")
        List<String> tags
) {

    public CreatePostCommand createPostCommand(Long userId){
        return new CreatePostCommand(userId, title(), content(), contentImg(), tags());
    }
}
