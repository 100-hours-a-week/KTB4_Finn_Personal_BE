package kr.ktb.finn_week6.domain.user.service;

import kr.ktb.finn_week6.domain.post.Post;
import kr.ktb.finn_week6.domain.post.repository.PostRepository;
import kr.ktb.finn_week6.domain.user.User;
import kr.ktb.finn_week6.domain.user.dto.LoginResult;
import kr.ktb.finn_week6.domain.user.dto.TokenInfo;
import kr.ktb.finn_week6.domain.user.dto.TokenResult;
import kr.ktb.finn_week6.domain.user.dto.command.*;
import kr.ktb.finn_week6.domain.user.dto.response.CreateUserResponse;
import kr.ktb.finn_week6.domain.user.dto.response.LoginUserResponse;
import kr.ktb.finn_week6.domain.user.dto.response.UserDetailResponse;
import kr.ktb.finn_week6.domain.user.repository.RefreshTokenRepository;
import kr.ktb.finn_week6.domain.user.repository.UserRepository;
import kr.ktb.finn_week6.global.RequestMessage;
import kr.ktb.finn_week6.global.customException.*;
import kr.ktb.finn_week6.global.dto.NotFoundException;
import kr.ktb.finn_week6.security.auth.JwtProvider;
import kr.ktb.finn_week6.security.entity.RefreshToken;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public CreateUserResponse register(CreateUserCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new DuplicateEmailException(RequestMessage.DUPLICATE_EMAIL.getDescription());
        }
        User user = new User(
                command.nickname(),
                command.email(),
                passwordEncoder.encode(command.password()),
                command.profileImg()
        );
        userRepository.save(user);
        return new CreateUserResponse(user.getId(), user.getNickname());
    }

    @Transactional
    public LoginResult login(LoginUserCommand command){
        User user = userRepository.findByEmail(command.email()).orElseThrow(
                () -> new NoSuchEmailException(RequestMessage.NOT_FOUND_EMAIL.getDescription())
        );
        if(user.isDeleted()){
            throw new DeletedUserException(RequestMessage.UNAUTHORIZED.getDescription());
        }

        if(!passwordEncoder.matches(command.password(), user.getPassword())){
            throw new IncorrectPasswordException(RequestMessage.INVALID_PASSWORD.getDescription());
        }

        String accessToken = jwtProvider.createAccessToken(
                user.getId(),
                user.getEmail(),
                user.getNickname()
        );

        String refreshToken = jwtProvider.createRefreshToken(user.getId());
        refreshTokenRepository.deleteByUserId(user.getId());
        refreshTokenRepository.save(
                new RefreshToken(
                        refreshToken,
                        user.getId(),
                        LocalDateTime.now().plusDays(14)
                )
        );
        TokenInfo tokenInfo = new TokenInfo(accessToken, jwtProvider.getAccessTokenValidityInMilliseconds()); //accessToken이랑 만료일

        LoginUserResponse loginUserResponse = LoginUserResponse.createLoginUserResponse(user, tokenInfo);

        return new LoginResult(loginUserResponse,refreshToken);
    }

    public UserDetailResponse getUserDetail(UserDetailCommand command){
        User targetUser = userRepository.findById(command.loginUserId()).orElseThrow(
                () -> new NotFoundException(RequestMessage.NOT_FOUND_USER.getDescription())
        );
        if(targetUser.isDeleted()){
            throw new DeletedUserException(RequestMessage.NOT_FOUND.getDescription());
        }

        return new UserDetailResponse(targetUser.getId(), targetUser.getNickname(), targetUser.getEmail(), targetUser.getProfileImg());
    }

    @Transactional
    public UserDetailResponse updateUserDetail(UpdateUserCommand command){
        User targetUser = userRepository.findById(command.loginUserId()).orElseThrow(
                () -> new NotFoundException(RequestMessage.NOT_FOUND_USER.getDescription())
        );
        if(targetUser.isDeleted()){
            throw new DeletedUserException(RequestMessage.NOT_FOUND.getDescription());
        }
        targetUser.updateUser(command.nickname(), command.profileImg());
        return new UserDetailResponse(targetUser.getId(), targetUser.getNickname(), targetUser.getEmail(), targetUser.getProfileImg());
    }

    @Transactional
    public void updatePassword(UpdatePasswordCommand command){
        User targetUser = userRepository.findById(command.loginUserId()).orElseThrow(
                () -> new NotFoundException(RequestMessage.NOT_FOUND_USER.getDescription())
        );
        if(targetUser.isDeleted()){
            throw new DeletedUserException(RequestMessage.NOT_FOUND.getDescription());
        }
        String encodedPassword = passwordEncoder.encode(command.password());
        targetUser.updatePassword(encodedPassword);
    }

    @Transactional
    public void deleteUser(DeleteUserCommand command){
        User targetUser = userRepository.findById(command.loginUserId()).orElseThrow(
                () -> new NotFoundException(RequestMessage.NOT_FOUND_USER.getDescription())
        );
        targetUser.setDeleted();
        List<Post> posts = postRepository.findByUserId(targetUser.getId());
        for (Post post : posts) {
            post.setDeleted();
        }
    }

    @Transactional
    public TokenResult refreshAccessToken(String refreshToken){
        System.out.println("UserService refreshAccessToken:");
        RefreshToken saved = refreshTokenRepository.findByToken(refreshToken).orElseThrow(
                () -> new AuthorizedException(RequestMessage.UNAUTHORIZED.getDescription())
        );
        if(saved.isExpired()){
            refreshTokenRepository.delete(saved);
            System.out.println("Delete RefreshToken:");
            throw new AuthorizedException(RequestMessage.UNAUTHORIZED.getDescription());
        }
        User user = userRepository.findById(saved.getUserId()).orElseThrow(
                () -> new AuthorizedException(RequestMessage.UNAUTHORIZED.getDescription())
        );

        String newAccessToken = jwtProvider.createAccessToken(
                user.getId(),
                user.getEmail(),
                user.getNickname()
        );
        System.out.println("newAccessToken: " + newAccessToken);

        // Refresh Token 회전 (Rotation)
        String newRefreshToken = jwtProvider.createRefreshToken(user.getId());
        refreshTokenRepository.delete(saved);
        refreshTokenRepository.save(new RefreshToken(
                newRefreshToken,
                user.getId(),
                LocalDateTime.now().plusDays(14)
                )
        );

        return new TokenResult(
                new TokenInfo(newAccessToken, jwtProvider.getAccessTokenValidityInMilliseconds()),
                newRefreshToken
        );
    }

    @Transactional
    public void deleteRefreshToken(Long userId, String refreshToken){
        RefreshToken saved = refreshTokenRepository.findByToken(refreshToken).orElseThrow(
                () -> new AuthorizedException(RequestMessage.UNAUTHORIZED.getDescription())
        );
        refreshTokenRepository.delete(saved);
    }

}
