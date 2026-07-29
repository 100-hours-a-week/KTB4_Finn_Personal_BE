package kr.ktb.finn_week6.domain.post.dto.response;

import kr.ktb.finn_week6.domain.post.Post;

import java.util.List;

public record UpdatePostResponse(
        Long id,
        String title,
        String content,
        String contentImg,
        List<String> tags
) {
    public static UpdatePostResponse createResponse(Post post, List<String> tags) {
        return new UpdatePostResponse(post.getId(), post.getTitle(), post.getContent(), post.getContentImg(), tags);
    }
}
