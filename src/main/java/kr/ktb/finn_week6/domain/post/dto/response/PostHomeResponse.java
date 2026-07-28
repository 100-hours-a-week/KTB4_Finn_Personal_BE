package kr.ktb.finn_week6.domain.post.dto.response;

import java.util.List;


public record PostHomeResponse(
        List<PostResponse> posts
) {
    public static PostHomeResponse createPostListResponse(List<PostResponse> postResponses){
        return new PostHomeResponse(postResponses);
    }
}
