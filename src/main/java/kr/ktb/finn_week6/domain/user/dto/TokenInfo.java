package kr.ktb.finn_week6.domain.user.dto;

//발급된 액세스 토큰과 그 만료 정보를 함께 전달하기 위한 응답용 DTO


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenInfo{
    private String accessToken;
    private long expiresIn;
}
