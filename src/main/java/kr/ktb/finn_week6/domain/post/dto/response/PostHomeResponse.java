package kr.ktb.finn_week6.domain.post.dto.response;

import java.util.List;


public record PostHomeResponse(
        List<PostResponse> posts,
        List<MostViewPostResponse> mostViewPosts
) {
    public static PostHomeResponse createPostListResponse(List<PostResponse> postResponses, List<MostViewPostResponse> mostViewPosts){
        return new PostHomeResponse(postResponses, mostViewPosts);
    }
}
