package kr.ktb.finn_week6.security.auth;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.sql.Date;
import java.time.Instant;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtProvider {
    private final JwtProperties jwtProperties;
    private Key key; // JWT 서명할 떄 사용할 비밀키 객체

    @PostConstruct//Bean 생성 후 자동 실행
    public void init(){//문자열로 된 secret 키 바이트 배열로 바꾼 후 JWT 서명용 키로 변환
        this.key = Keys.hmacShaKeyFor(
                jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8) //application.properties에 선언된 secetet key가져와 문자열을 UTF-8 기준 바이트 배열 변환 휴 hmac-sha 알고릐즘에 사용할 수 있는 secretkey 생성
                //=> JWT 서명에 사용할 비밀키를 미리 만들어두는 코드
        );
    }

    private String createToken( // 공통 토큰 생성 메서드
            String type, //토큰 종류
            Long userId,
            Map<String, Object> claims, //JWT에 추가로 넣을 정보 => 이메일, 닉네임, 권한...
            long expSeconds // 토큰 만료 시간
    ){
        Instant now =  Instant.now(); //현재 시간
        return Jwts.builder()// JWT 생성 위한 builder 시작
                .subject(String.valueOf(userId)) //JWT의 sub에 사용자 ID 삽인 =============> sub이 뭔데
                .claim("typ", type)//JWT안에 typ라는 claims 추가. 뒤에 type은 토큰 종류 뜻하는 거 아닌가?????
                .claims(claims)//추가 claims들 creaetAccessToken에서 Map.of에 있는 정보들
                .issuedAt(Date.from(now))//JWT발급 시간
                .expiration(Date.from(now.plusSeconds(expSeconds)))//JWT만료시간
                .signWith((SecretKey) key, Jwts.SIG.HS256)//서명
                .compact();//jwt 문자열 생성
    }

    public String createAccessToken(Long userId, String email, String nickname){ //accesstoekn 생성하는 메서드
        return createToken(
                "access",
                userId,
                Map.of("email", email, "nickname", nickname),
                jwtProperties.getAccessTokenExpSeconds()
        );
    }

    public String createRefreshToken(Long userId){//refreshTOken 생성하는 메서드
        return createToken(
                "refresh",
                userId,
                Map.of(),
                jwtProperties.getRefreshTokenExpSeconds()
        );
    }

    public Jws<Claims> parseToken(String token){ //JWT 문자열 검증하고 내부 정보(claims)를 꺼내는 메서드
        return Jwts.parser()//jwt 읽기 위한 파서
                .verifyWith((SecretKey) key) //서명 인증???key는 서버에서 가지고 있는 거 아닌가
                .build()
                .parseSignedClaims(token);//토큰 파싱 -> header + payload + signature로 디코딩
    }

    public boolean isAccessToken(String token) {
        return "access".equals(parseToken(token).getPayload().get("typ", String.class));
    }

    public Long getUserId(String token) {
        return Long.valueOf(parseToken(token).getPayload().getSubject());
    }

    public Long getAccessTokenValidityInMilliseconds() {
        return jwtProperties.getAccessTokenExpSeconds() * 1000;
    }

}
