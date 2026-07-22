package kr.ktb.finn_week6.domain.post.dto.command;

public record UpdatePostCommand(
        Long userId,
        Long postId,
        String title,
        String content,
        String contentImg
) {

}
