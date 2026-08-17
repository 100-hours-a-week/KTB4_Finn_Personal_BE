package kr.ktb.finn_week6.domain.post.dto.response;

import java.time.LocalDateTime;
import java.util.List;


public record PostListResponse(
        List<PostResponse> posts,
        Integer cursorLikeCount,
        LocalDateTime cursorCreatedAt,
        Long cursorId,
        boolean hasNext

) {
    public static PostListResponse createPostListResponse(List<PostResponse> postResponses, Integer cursorLikeCount ,LocalDateTime cursorCreatedAt, Long cursorId, boolean hasNext){
        return new PostListResponse(
                postResponses,
                cursorLikeCount,
                cursorCreatedAt,
                cursorId,
                hasNext);
    }
}
