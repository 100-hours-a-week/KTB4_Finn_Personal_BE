package kr.ktb.finn_week6.domain.like.dto.response;

public record LikeResponse(
        Long likeId,
        boolean isLiked,
        int likeCount
){

}
