package kr.ktb.finn_week6.domain.post.dto.response;

import java.time.LocalDateTime;
import java.util.List;


public record PostListResponse(
        List<PostResponse> posts,
        LocalDateTime nextCursorCreatedAt,
        Long nextCursorId,
        boolean hasNext

) {
    public static PostListResponse createPostListResponse(List<PostResponse> postResponses, LocalDateTime nextCursorCreatedAt, Long nextCursorId, boolean hasNext){
        return new PostListResponse(postResponses, nextCursorCreatedAt, nextCursorId, hasNext);
    }
}
