package kr.ktb.finn_week6.domain.post.dto.response;

import kr.ktb.finn_week6.domain.post.Post;

import java.time.LocalDateTime;

public record PostResponse(
        Long id,
        String title,
        String content,
        LocalDateTime createdAt,
        String contentImg,
        String nickname,
        String profileImg,
        boolean isLiked,
        int likeCount,
        int commentCount,
        int viewCount
) {
    public static PostResponse createPostResponse(Post post, boolean isLiked) {
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getCreatedAt(),
                post.getContentImg(),
                post.getUser().getNickname(),
                post.getUser().getProfileImg(),
                isLiked,
                post.getLikeCount(),
                post.getCommentCount(),
                post.getViewCount());
    }

}
