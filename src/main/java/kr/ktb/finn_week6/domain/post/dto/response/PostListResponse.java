package kr.ktb.finn_week6.domain.post.dto.response;

import java.util.List;


public record PostListResponse(
        List<PostResponse> posts
) {
    public static PostListResponse createPostListResponse(List<PostResponse> postResponses){
        return new PostListResponse(postResponses);
    }
}
