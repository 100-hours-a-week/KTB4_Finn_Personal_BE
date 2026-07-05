package kr.ktb.finn_week6.domain.user.dto.response;

import kr.ktb.finn_week6.domain.user.User;
import kr.ktb.finn_week6.domain.user.dto.TokenInfo;

public record LoginUserResponse(
        Long id,
        String nickname,
        String email,
        String profileImg,
        TokenInfo token
) {
    public static LoginUserResponse createLoginUserResponse(User user, TokenInfo accessToken){
        return new LoginUserResponse(user.getId(), user.getNickname(),user.getEmail(), user.getProfileImg(),accessToken);
    }
}
