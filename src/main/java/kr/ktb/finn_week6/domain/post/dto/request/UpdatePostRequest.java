package kr.ktb.finn_week6.domain.post.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import kr.ktb.finn_week6.domain.place.dto.request.PostLocationRequest;
import kr.ktb.finn_week6.domain.post.dto.command.UpdatePostCommand;

import java.util.List;

public record UpdatePostRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 26, message = "Title must be less than 26 characters")
        String title,
        @NotBlank(message = "Content is required")
        String content,
        String contentImg,

        @Size(max = 5, message = "Tags must be less than 5")
        List<@NotBlank @Size(max = 20, message = "Tag must be less than 20")
                String> tags,
        @Valid
        PostLocationRequest location

) {
    public UpdatePostCommand toCommand(Long userId, Long postId, PostLocationRequest location){

        return new UpdatePostCommand(userId,postId,title(),content(),contentImg(), tags(), location);
    }
}
