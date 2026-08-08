package kr.ktb.finn_week6.domain.post.dto.command;

import kr.ktb.finn_week6.domain.place.dto.request.PostLocationRequest;

import java.util.List;

public record UpdatePostCommand(
        Long userId,
        Long postId,
        String title,
        String content,
        String contentImg,
        List<String> tags,
        PostLocationRequest location
) {

}
