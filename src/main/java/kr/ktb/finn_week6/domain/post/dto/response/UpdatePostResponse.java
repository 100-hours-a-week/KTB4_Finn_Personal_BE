package kr.ktb.finn_week6.domain.post.dto.response;

import kr.ktb.finn_week6.domain.place.dto.PostPlaceInfo;
import kr.ktb.finn_week6.domain.post.Post;

import java.util.List;

public record UpdatePostResponse(
        Long id,
        String title,
        String content,
        String contentImg,
        List<String> tags,
        PostPlaceInfo location
) {
    public static UpdatePostResponse createResponse(Post post, List<String> tags, PostPlaceInfo location) {
        return new UpdatePostResponse(post.getId(), post.getTitle(), post.getContent(), post.getContentImg(), tags, location);
    }
}
