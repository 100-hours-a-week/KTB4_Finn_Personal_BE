package kr.ktb.finn_week6.security.auth;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


/*
application.properties에서 정의한 JWT 설정 값 바인딩하기 위한 설정 클래스
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secretKey;
    private long accessTokenExpSeconds;
    private long refreshTokenExpSeconds;
}
