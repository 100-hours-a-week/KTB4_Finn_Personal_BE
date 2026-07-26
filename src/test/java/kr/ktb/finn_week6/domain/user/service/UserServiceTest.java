package kr.ktb.finn_week6.domain.user.service;

import kr.ktb.finn_week6.domain.post.Post;
import kr.ktb.finn_week6.domain.post.repository.PostRepository;
import kr.ktb.finn_week6.domain.user.User;
import kr.ktb.finn_week6.domain.user.dto.LoginResult;
import kr.ktb.finn_week6.domain.user.dto.TokenResult;
import kr.ktb.finn_week6.domain.user.dto.command.*;
import kr.ktb.finn_week6.domain.user.dto.response.CreateUserResponse;
import kr.ktb.finn_week6.domain.user.dto.response.UserDetailResponse;
import kr.ktb.finn_week6.domain.user.repository.RefreshTokenRepository;
import kr.ktb.finn_week6.domain.user.repository.UserRepository;
import kr.ktb.finn_week6.global.customException.*;
import kr.ktb.finn_week6.global.dto.NotFoundException;
import kr.ktb.finn_week6.security.auth.JwtProvider;
import kr.ktb.finn_week6.security.entity.RefreshToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PostRepository postRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("유저 등록 시, 중복 이메일이 존재할 경우 DuplicateEmailException이 발생한다.")
    void registerUserDuplicateEmail(){
        // given
        CreateUserCommand command = new CreateUserCommand(
                "TestUser",
                "test@test.com",
                "test",
                "https://test.com/test.png"
        );

        when(userRepository.existsByEmail(command.email())).thenReturn(true);

        // when & then
        assertThrows(DuplicateEmailException.class, () -> userService.register(command));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("유저 등록 시, 중복 이메일이 없을 경우 유저 생성/저장이 성공한다.")
    void registerUserSuccess() {
        // given
        CreateUserCommand command = new CreateUserCommand(
                "TestUser",
                "test@test.com",
                "test",
                "https://test.com/test.png"
        );

        when(userRepository.existsByEmail(command.email())).thenReturn(false);
        when(passwordEncoder.encode(command.password())).thenReturn("encodedPassword");

        doAnswer(invocation -> {
            User user = invocation.getArgument(0);
            ReflectionTestUtils.setField(user, "id", 1L);
            return null;
        }).when(userRepository).save(any(User.class));

        //when
        CreateUserResponse result = userService.register(command);


        // then
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userArgumentCaptor.capture());

        User savedUser = userArgumentCaptor.getValue();

        assertThat(savedUser.getNickname()).isEqualTo(command.nickname());
        assertThat(savedUser.getEmail()).isEqualTo(command.email());
        assertThat(savedUser.getPassword()).isEqualTo("encodedPassword");
        assertThat(savedUser.getProfileImg()).isEqualTo(command.profileImg());
        assertThat(savedUser.isDeleted()).isFalse();

        assertThat(result.nickname()).isEqualTo(command.nickname());
        assertThat(result.id()).isEqualTo(1L);

        verify(userRepository).existsByEmail(command.email());
        verify(passwordEncoder).encode(command.password());
    }

    @Test
    @DisplayName("로그인 시, 해당 이메일을 가진 사용자 없으면 NoSuchEmailException 발생한다.")
    void loginUserNotFound() {
        //given
        LoginUserCommand loginUserCommand = new LoginUserCommand("test@test.com", "test");
        when(userRepository.findByEmail(loginUserCommand.email())).thenReturn(Optional.empty());

        //when&then
        assertThrows(NoSuchEmailException.class, ()  -> userService.login(loginUserCommand));

        verify(userRepository).findByEmail(loginUserCommand.email());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtProvider, never()).createAccessToken(anyLong(), anyString(), anyString());
        verify(jwtProvider, never()).createRefreshToken(anyLong());
    }

    @Test
    @DisplayName("로그인 시, 해당 사용자 탈퇴한 사용자일 경우 DeletedUserException 발생한다.")
    void loginUserDeleted() {
        //given
        LoginUserCommand loginUserCommand = new LoginUserCommand("test@test.com", "test");

        User user = new User(
                "TestUser",
                "test@test.com",
                "test",
                "https://test.com/test.png"
        );

        user.setDeleted();

        when(userRepository.findByEmail(loginUserCommand.email())).thenReturn(Optional.of(user));

        //when&then
        assertThrows(DeletedUserException.class, ()  -> userService.login(loginUserCommand));

        verify(userRepository).findByEmail(loginUserCommand.email());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtProvider, never()).createAccessToken(anyLong(), anyString(), anyString());
        verify(jwtProvider, never()).createRefreshToken(anyLong());
    }

    @Test
    @DisplayName("로그인 시, 비밀번호 불일치 시 IncorrectPasswordException 발생한다.")
    void loginUserIncorrectPassword() {
        //given
        LoginUserCommand loginUserCommand = new LoginUserCommand("test@test.com", "test");

        User user = new User(
                "TestUser",
                "test@test.com",
                "encodedPassword",
                "https://test.com/test.png"
        );
        when(userRepository.findByEmail(loginUserCommand.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(loginUserCommand.password(), user.getPassword())).thenReturn(false);
        //when & then
        assertThrows(IncorrectPasswordException.class, () -> userService.login(loginUserCommand));
        verify(userRepository).findByEmail(loginUserCommand.email());
        verify(passwordEncoder).matches(loginUserCommand.password(), user.getPassword());
        verify(jwtProvider, never()).createAccessToken(anyLong(), anyString(), anyString());
        verify(jwtProvider, never()).createRefreshToken(anyLong());
        verify(refreshTokenRepository, never()).deleteByUserId(anyLong());
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));

    }

    @Test
    @DisplayName("로그인 시, 해당 이메일 존재하고 탈퇴한 사용자가 아니라면 AccessToken을 생성하고 기존 refreshToken존재할 경우 삭제하고 RefreshToken을 생성한다.")
    void loginUserSuccess() {
        // given
        LoginUserCommand loginUserCommand = new LoginUserCommand("test@test.com", "test");

        User user = new User(
                "TestUser",
                "test@test.com",
                "encodedPassword",
                "https://test.com/test.png"
        );
        ReflectionTestUtils.setField(user, "id", 1L);

        when(userRepository.findByEmail(loginUserCommand.email()))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(loginUserCommand.password(), user.getPassword()))
                .thenReturn(true);

        when(jwtProvider.createAccessToken(user.getId(), user.getEmail(), user.getNickname()))
                .thenReturn("accessToken");

        when(jwtProvider.createRefreshToken(user.getId()))
                .thenReturn("refreshToken");

        when(jwtProvider.getAccessTokenValidityInMilliseconds())
                .thenReturn(3600000L);

        // when
        LoginResult result = userService.login(loginUserCommand);

        // then
        verify(userRepository).findByEmail(loginUserCommand.email());
        verify(passwordEncoder).matches(loginUserCommand.password(), user.getPassword());
        verify(jwtProvider).createAccessToken(user.getId(), user.getEmail(), user.getNickname());
        verify(jwtProvider).createRefreshToken(user.getId());
        verify(refreshTokenRepository).deleteByUserId(user.getId());

        ArgumentCaptor<RefreshToken> refreshTokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(refreshTokenCaptor.capture());

        RefreshToken savedRefreshToken = refreshTokenCaptor.getValue();

        assertThat(savedRefreshToken.getToken()).isEqualTo("refreshToken");
        assertThat(savedRefreshToken.getUserId()).isEqualTo(user.getId());
        assertThat(savedRefreshToken.getExpiredAt()).isAfter(LocalDateTime.now());

        assertThat(result.getRefreshToken()).isEqualTo("refreshToken");
        assertThat(result.getResponse().id()).isEqualTo(user.getId());
        assertThat(result.getResponse().nickname()).isEqualTo(user.getNickname());
        assertThat(result.getResponse().email()).isEqualTo(user.getEmail());
        assertThat(result.getResponse().profileImg()).isEqualTo(user.getProfileImg());
        assertThat(result.getResponse().token().getAccessToken()).isEqualTo("accessToken");
        assertThat(result.getResponse().token().getExpiresIn()).isEqualTo(3600000L);
    }

    @Test
    @DisplayName("유저 상세 정보 조회 시, 해당 유저가 없을 경우 NotFoundException이 발생한다.")
    void getUserDetailUserNotFound() {
        // given
        UserDetailCommand command = new UserDetailCommand(1L);

        when(userRepository.findById(command.loginUserId())).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class,
                () -> userService.getUserDetail(command));

        verify(userRepository).findById(command.loginUserId());
    }
    @Test
    @DisplayName("유저 상세 정보 조회 시, 탈퇴한 유저일 경우 DeletedUserException이 발생한다.")
    void getUserDetailUserDeleted() {
        //given
        UserDetailCommand command = new UserDetailCommand(1L);
        User user = new User(
                "TestUser",
                "test@test.com",
                "encodedPassword",
                "https://test.com/test.png"
        );

        user.setDeleted();
        when(userRepository.findById(command.loginUserId())).thenReturn(Optional.of(user));
        //when & then
        assertThrows(DeletedUserException.class,
                () -> userService.getUserDetail(command));
        verify(userRepository).findById(command.loginUserId());

    }

    @Test
    @DisplayName("유저 상세 정보 조회 시, 해당 유저가 존재하면 성공한다.")
    void getUserDetailUserFound(){
        //given
        UserDetailCommand command = new UserDetailCommand(1L);
        User user = new User(
                "TestUser",
                "test@test.com",
                "test",
                "https://test.com/test.png"
        );


        when(userRepository.findById(command.loginUserId())).thenReturn(Optional.of(user));
        //when

        UserDetailResponse result = userService.getUserDetail(command);

        //then
        verify(userRepository).findById(command.loginUserId());

        assertThat(result.nickname()).isEqualTo(user.getNickname());
        assertThat(result.email()).isEqualTo(user.getEmail());
        assertThat(result.profileImg()).isEqualTo(user.getProfileImg());
    }


    @Test
    @DisplayName("유저 정보 수정 시, 해당 유저 없으면 NotFoundException 발생한다.")
    void updateUserDetailNotFound() {
        // given
        UpdateUserCommand command = new UpdateUserCommand(1L, "newName", "https://new.com/profile.png");

        when(userRepository.findById(command.loginUserId()))
                .thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class,
                () -> userService.updateUserDetail(command));

        verify(userRepository).findById(command.loginUserId());
    }
    @Test
    @DisplayName("유저 정보 수정 시, 해당 유저 탈퇴한 경우 DeletedUserException 발생한다.")
    void updateUserDetailDeleted() {
        // given
        UpdateUserCommand command = new UpdateUserCommand(1L, "newName", "https://new.com/profile.png");

        User user = new User(
                "oldName",
                "test@test.com",
                "test",
                "https://old.com/profile.png"
        );
        user.setDeleted();

        when(userRepository.findById(command.loginUserId()))
                .thenReturn(Optional.of(user));

        // when & then
        assertThrows(DeletedUserException.class,
                () -> userService.updateUserDetail(command));

        verify(userRepository).findById(command.loginUserId());
    }

    @Test
    @DisplayName("유저 정보 수정 시, 해당 유저 존재&미탈퇴 시 회원정보 수정이 수행된다.")
    void updateUserDetailSuccess() {
        // given
        UpdateUserCommand command = new UpdateUserCommand(1L, "newName", "https://new.com/profile.png");

        User user = new User(
                "oldName",
                "test@test.com",
                "test",
                "https://old.com/profile.png"
        );

        when(userRepository.findById(command.loginUserId()))
                .thenReturn(Optional.of(user));

        // when
        UserDetailResponse result = userService.updateUserDetail(command);

        // then
        verify(userRepository).findById(command.loginUserId());

        assertThat(user.getNickname()).isEqualTo(command.nickname());
        assertThat(user.getProfileImg()).isEqualTo(command.profileImg());

        assertThat(result.nickname()).isEqualTo(command.nickname());
        assertThat(result.profileImg()).isEqualTo(command.profileImg());
    }

    @Test
    @DisplayName("유저 정보 수정 시, 닉네임만 수정할 경우 기존 프로필 이미지는 유지된다.")
    void updateUserDetailOnlyNickname() {
        // given
        UpdateUserCommand command = new UpdateUserCommand(1L, "newName", null);

        User user = new User(
                "oldName",
                "test@test.com",
                "test",
                "https://old.com/profile.png"
        );

        when(userRepository.findById(command.loginUserId()))
                .thenReturn(Optional.of(user));

        // when
        UserDetailResponse result = userService.updateUserDetail(command);

        // then
        assertThat(user.getNickname()).isEqualTo("newName");
        assertThat(user.getProfileImg()).isEqualTo("https://old.com/profile.png");

        assertThat(result.nickname()).isEqualTo("newName");
        assertThat(result.profileImg()).isEqualTo("https://old.com/profile.png");
    }

    @Test
    @DisplayName("비밀번호 수정 시, 해당 유저 없으면 NotFoundException 발생한다.")
    void updatePasswordUserNotFound() {
        //given
        UpdatePasswordCommand command = new UpdatePasswordCommand(1L, "currentPassword", "newPassword");
        when(userRepository.findById(command.loginUserId())).thenReturn(Optional.empty());

        //when & then
        assertThrows(NotFoundException.class, () -> userService.updatePassword(command));
        verify(userRepository).findById(command.loginUserId());
    }

    @Test
    @DisplayName("비밀번호 수정 시, 해당 유저 탈퇴했으면 DeletedUserException 발생한다.")
    void updatePasswordUserDeleted() {
        //given
        UpdatePasswordCommand command = new UpdatePasswordCommand(1L, "currentPassword","newPassword");
        User user = new User(
                "TestUser",
                "test@test.com",
                "oldPassword",
                "https://test.com/test.png"
        );
        user.setDeleted();

        when(userRepository.findById(command.loginUserId())).thenReturn(Optional.of(user));

        //when & then
        assertThrows(DeletedUserException.class, () -> userService.updatePassword(command));
        verify(userRepository).findById(command.loginUserId());
    }

    @Test
    @DisplayName("현재 비밀번호가 일치하지 않으면 비밀번호를 변경하지 않는다.")
    void updatePasswordCurrentPasswordMismatch() {
        UpdatePasswordCommand command =
                new UpdatePasswordCommand(1L, "wrongPassword", "newPassword");

        User user = new User(
                "TestUser",
                "test@test.com",
                "encodedCurrentPassword",
                "https://test.com/test.png"
        );

        when(userRepository.findById(command.loginUserId()))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                command.currentPassword(),
                user.getPassword()
        )).thenReturn(false);

        assertThrows(
                CurrentPasswordMismatch.class,
                () -> userService.updatePassword(command)
        );

        assertThat(user.getPassword()).isEqualTo("encodedCurrentPassword");
        verify(passwordEncoder, never()).encode(anyString());
    }
    @Test
    @DisplayName("현재 비밀번호가 일치하면 새로운 비밀번호로 변경한다.")
    void updatePasswordSuccess() {
        UpdatePasswordCommand command =
                new UpdatePasswordCommand(1L, "currentPassword", "newPassword");

        User user = new User(
                "TestUser",
                "test@test.com",
                "encodedCurrentPassword",
                "https://test.com/test.png"
        );

        when(userRepository.findById(command.loginUserId()))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                command.currentPassword(),
                user.getPassword()
        )).thenReturn(true);

        when(passwordEncoder.encode(command.newPassword()))
                .thenReturn("encodedNewPassword");

        userService.updatePassword(command);

        assertThat(user.getPassword()).isEqualTo("encodedNewPassword");

        verify(passwordEncoder).matches(
                command.currentPassword(),
                "encodedCurrentPassword"
        );
        verify(passwordEncoder).encode(command.newPassword());
    }

    @Test
    @DisplayName("유저 삭제 시, 해당 유저가 없을 경우 NotFoundException이 발생한다.")
    void deleteUserNotFound() {
        // given
        DeleteUserCommand command = new DeleteUserCommand(1L);

        when(userRepository.findById(command.loginUserId())).thenReturn(Optional.empty());

        // when & then
        assertThrows(NotFoundException.class, () -> userService.deleteUser(command));

        verify(userRepository).findById(command.loginUserId());
        verify(postRepository, never()).findByUserId(command.loginUserId());
    }

    @Test
    @DisplayName("유저 삭제 시, 해당 유저가 존재하면 삭제에 성공한다.")
    void deleteUser(){
        //given
        DeleteUserCommand command = new DeleteUserCommand(1L);

        User user = new User(
                "TestUser",
                "test@test.com",
                "test",
                "https://test.com/test.png"
        );

        Post postA = new Post(user, "titleA", "contentB", "https://test.com/test.png");
        Post postB = new Post(user, "titleB", "contentB", "https://test.com/test.png");

        ReflectionTestUtils.setField(user, "id", 1L);

        when(userRepository.findById(command.loginUserId())).thenReturn(Optional.of(user));
        when(postRepository.findByUserId(command.loginUserId())).thenReturn(List.of(postA, postB));

        //when
        userService.deleteUser(command);

        //then
        verify(userRepository).findById(command.loginUserId());
        verify(postRepository).findByUserId(user.getId());

        assertThat(user.isDeleted()).isTrue();
        assertThat(postA.isDeleted()).isTrue();
        assertThat(postB.isDeleted()).isTrue();
    }


    @Test
    @DisplayName("refreshToken 재발급 시, 해당 refreshToken 없을 경우 AuthorizedException 발생한다.")
    void invalidRefreshToken(){
        //given
        String refreshToken = "invalidRefreshToken";

        when(refreshTokenRepository.findByToken(refreshToken)).thenReturn(Optional.empty());

        //when & then
        assertThrows(AuthorizedException.class, () -> userService.refreshAccessToken(refreshToken));

        verify(refreshTokenRepository).findByToken(refreshToken);
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
        verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
        verify(jwtProvider, never()).createAccessToken(anyLong(), anyString(), anyString());
        verify(jwtProvider, never()).createRefreshToken(anyLong());
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("refreshToken 재발급 시, 해당 refreshToken 만료 시 AuthorizedException 발생한다.")
    void expiredRefreshToken(){
        //given
        String refreshToken = "expiredRefreshToken";

        RefreshToken savedRefreshToken = new RefreshToken(
                refreshToken,
                1L,
                LocalDateTime.now().minusDays(1)
        );

        when(refreshTokenRepository.findByToken(refreshToken)).thenReturn(Optional.of(savedRefreshToken));

        //when
        assertThrows(AuthorizedException.class, () -> userService.refreshAccessToken(refreshToken));

        //then
        verify(refreshTokenRepository).findByToken(refreshToken);
        verify(refreshTokenRepository).delete(savedRefreshToken);
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
        verify(jwtProvider, never()).createAccessToken(anyLong(), anyString(), anyString());
        verify(jwtProvider, never()).createRefreshToken(anyLong());
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("refreshToken 재발급 시, 유효한 refreshToken이면 accessToken과 refreshToken을 재발급한다.")
    void refreshTokenSuccess(){
        // given
        String refreshToken = "oldRefreshToken";
        RefreshToken savedRefreshToken = new RefreshToken(
                refreshToken,
                1L,
                LocalDateTime.now().plusDays(1)
        );

        User user = new User(
                "TestUser",
                "test@test.com",
                "encodedPassword",
                "https://test.com/test.png"
        );
        ReflectionTestUtils.setField(user, "id", 1L);

        when(refreshTokenRepository.findByToken(refreshToken))
                .thenReturn(Optional.of(savedRefreshToken));
        when(userRepository.findById(savedRefreshToken.getUserId()))
                .thenReturn(Optional.of(user));
        when(jwtProvider.createAccessToken(user.getId(), user.getEmail(), user.getNickname()))
                .thenReturn("newAccessToken");
        when(jwtProvider.createRefreshToken(user.getId()))
                .thenReturn("newRefreshToken");
        when(jwtProvider.getAccessTokenValidityInMilliseconds())
                .thenReturn(3_600_000L);

        // when
        TokenResult result = userService.refreshAccessToken(refreshToken);

        // then
        verify(refreshTokenRepository).findByToken(refreshToken);
        verify(userRepository).findById(savedRefreshToken.getUserId());
        verify(jwtProvider).createAccessToken(user.getId(), user.getEmail(), user.getNickname());
        verify(jwtProvider).createRefreshToken(user.getId());
        verify(refreshTokenRepository).delete(savedRefreshToken);

        ArgumentCaptor<RefreshToken> refreshTokenCaptor =
                ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(refreshTokenCaptor.capture());

        RefreshToken newSavedRefreshToken = refreshTokenCaptor.getValue();

        assertThat(newSavedRefreshToken.getToken()).isEqualTo("newRefreshToken");
        assertThat(newSavedRefreshToken.getUserId()).isEqualTo(user.getId());
        assertThat(newSavedRefreshToken.getExpiredAt()).isAfter(LocalDateTime.now());

        assertThat(result.getToken().getAccessToken()).isEqualTo("newAccessToken");
        assertThat(result.getToken().getExpiresIn()).isEqualTo(3_600_000L);
        assertThat(result.getNewRefreshToken()).isEqualTo("newRefreshToken");
    }

}
