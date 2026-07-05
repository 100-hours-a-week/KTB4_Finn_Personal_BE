package kr.ktb.finn_week6.domain.user.dto;

import kr.ktb.finn_week6.domain.user.dto.response.LoginUserResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
//로그인 처리 결과를 두가지 전달 경로로 분리하기 위한 DTO

@Getter
@AllArgsConstructor
public class LoginResult{
    private LoginUserResponse response;
    private String refreshToken;
}
