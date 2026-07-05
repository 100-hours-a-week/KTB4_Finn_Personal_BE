package kr.ktb.finn_week6.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

//토큰 발급 및 재발급 결과를 하나의 응답으로 전달하기 위한 객체

@Getter
@AllArgsConstructor
public class TokenResult {
    private TokenInfo token;
    private String newRefreshToken;
}