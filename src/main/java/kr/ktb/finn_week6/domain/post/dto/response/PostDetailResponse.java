package kr.ktb.finn_week6.domain.post.dto.response;

import kr.ktb.finn_week6.domain.comment.dto.response.CommentDetailResponse;
import kr.ktb.finn_week6.domain.hashtag.HashTag;
import kr.ktb.finn_week6.domain.like.Like;
import kr.ktb.finn_week6.domain.post.Post;

import java.time.LocalDateTime;
import java.util.List;

public record PostDetailResponse(
        Long postId,
        String title,
        String username,
        String profileImg,
        LocalDateTime createdAt,
        String contentImg,
        String content,
        int likeCount,
        int viewCount,
        int commentCount,
        boolean isMine,
        boolean isLiked,
        List<String> tagNames
) {
    public static PostDetailResponse createPostDetailResponse(Post post, boolean isMine, boolean isLiked, List<String> tagNames) {
        return new PostDetailResponse(
                post.getId(),
                post.getTitle(),
                post.getUser().getNickname(),
                post.getUser().getProfileImg(),
                post.getCreatedAt(),
                post.getContentImg(),
                post.getContent(),
                post.getLikeCount(),
                post.getViewCount(),
                post.getCommentCount(),
                isMine,
                isLiked,
                tagNames
        );

    }
}
