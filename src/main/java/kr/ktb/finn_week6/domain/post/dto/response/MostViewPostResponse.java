package kr.ktb.finn_week6.domain.post.dto.response;

import kr.ktb.finn_week6.domain.post.Post;

public record MostViewPostResponse(
        Long id,
        String title,
        String username
) {
    public static MostViewPostResponse createMostViewPostResponse(Post post){
        return new MostViewPostResponse(post.getId(), post.getTitle(), post.getUser().getNickname());
    }
}
