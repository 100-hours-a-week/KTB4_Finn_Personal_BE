package kr.ktb.finn_week6.domain.like.dto.command;

public record LikePostCommand(
        Long userId,
        Long postId
) {
    public static LikePostCommand createLikePostCommand(Long userId, Long postId) {
        return new LikePostCommand(userId, postId);
    }
}
