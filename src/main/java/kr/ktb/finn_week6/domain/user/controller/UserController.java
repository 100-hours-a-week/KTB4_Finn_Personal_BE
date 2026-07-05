package kr.ktb.finn_week6.domain.user.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import kr.ktb.finn_week6.domain.user.dto.LoginResult;
import kr.ktb.finn_week6.domain.user.dto.TokenInfo;
import kr.ktb.finn_week6.domain.user.dto.TokenResult;
import kr.ktb.finn_week6.domain.user.dto.command.*;
import kr.ktb.finn_week6.domain.user.dto.request.CreateUserRequest;
import kr.ktb.finn_week6.domain.user.dto.request.LoginUserRequest;
import kr.ktb.finn_week6.domain.user.dto.request.UpdatePasswordRequest;
import kr.ktb.finn_week6.domain.user.dto.request.UpdateUserRequest;
import kr.ktb.finn_week6.domain.user.dto.response.CreateUserResponse;
import kr.ktb.finn_week6.domain.user.dto.response.LoginUserResponse;
import kr.ktb.finn_week6.domain.user.dto.response.UserDetailResponse;
import kr.ktb.finn_week6.domain.user.repository.RefreshTokenRepository;
import kr.ktb.finn_week6.domain.user.service.UserService;
import kr.ktb.finn_week6.global.RequestMessage;
import kr.ktb.finn_week6.global.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final RefreshTokenRepository refreshTokenRepository;


    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CreateUserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        CreateUserCommand command = request.createUserCommand();
        CreateUserResponse createUserResponse = userService.register(command);
        return new ApiResponse<>(RequestMessage.SUCCESS.getDescription(), createUserResponse);
    }


    @GetMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<UserDetailResponse> getUserDetail(@AuthenticationPrincipal Long userId) {

        UserDetailCommand userDetailCommand = new UserDetailCommand(userId);
        UserDetailResponse userDetailResponse = userService.getUserDetail(userDetailCommand);

        return new ApiResponse<>(RequestMessage.SUCCESS.getDescription(), userDetailResponse);
    }

    @PatchMapping("/me")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<UserDetailResponse> updateUserDetail(@Valid @RequestBody UpdateUserRequest request, @AuthenticationPrincipal Long userId) {

        UpdateUserCommand updateUserCommand = request.updateUserCommand(userId);
        UserDetailResponse userDetailResponse = userService.updateUserDetail(updateUserCommand);

        return new ApiResponse<>(RequestMessage.SUCCESS.getDescription(), userDetailResponse);
    }

    @PatchMapping("/me/password")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> updateUserPassword(@Valid @RequestBody UpdatePasswordRequest request, @AuthenticationPrincipal Long userId){

        UpdatePasswordCommand command = request.toCommand(userId);
        userService.updatePassword(command);
        return new ApiResponse<>(RequestMessage.SUCCESS.getDescription(), null);
    }


    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<LoginUserResponse> login(@Valid @RequestBody LoginUserRequest request, HttpServletResponse response) {
        LoginUserCommand loginUserCommand = request.loginUserCommand();
        LoginResult result = userService.login(loginUserCommand);

        ResponseCookie refreshCookie = ResponseCookie
                .from("refreshToken", result.getRefreshToken())
                .httpOnly(true)
                .path("/")
                .maxAge(14 * 24 * 60 * 60)
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return new ApiResponse<>(RequestMessage.SUCCESS.getDescription(), result.getResponse());

    }

    @PostMapping("/token/refresh")
    public ApiResponse<TokenInfo> refreshToken(@CookieValue(name = "refreshToken", required = false) String refreshToken, HttpServletResponse response) {
        System.out.println("UserController refreshToken: " + refreshToken);
        TokenResult result = userService.refreshAccessToken(refreshToken);
        if (result.getNewRefreshToken() != null) {
            ResponseCookie cookie = ResponseCookie.from("refreshToken", result.getNewRefreshToken())
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(14 * 24 * 60 * 60)
                    .sameSite("Strict")
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        }
        return new ApiResponse<>(RequestMessage.TOKEN_REFRESH_SUCCESS.getDescription(), result.getToken());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> logout(@AuthenticationPrincipal Long userId, @CookieValue(name = "refreshToken", required = false) String refreshToken) {
        userService.deleteRefreshToken(userId, refreshToken);
        return new ApiResponse<>(RequestMessage.SUCCESS.getDescription(), null);
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@AuthenticationPrincipal Long userId, @CookieValue(name = "refreshToken", required = false) String refreshToken) {
        DeleteUserCommand deleteUserCommand = new DeleteUserCommand(userId);
        userService.deleteUser(deleteUserCommand);
        userService.deleteRefreshToken(userId,refreshToken);
    }

}
