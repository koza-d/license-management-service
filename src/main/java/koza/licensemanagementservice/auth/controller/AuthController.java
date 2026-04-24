package koza.licensemanagementservice.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import koza.licensemanagementservice.auth.dto.JwtTokenDTO;
import koza.licensemanagementservice.auth.dto.LoginResponse;
import koza.licensemanagementservice.auth.dto.MemberLoginRequest;
import koza.licensemanagementservice.auth.jwt.JwtTokenProvider;
import koza.licensemanagementservice.auth.service.RefreshTokenService;
import koza.licensemanagementservice.global.common.ApiResponse;
import koza.licensemanagementservice.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.Duration;

@Controller
@RequiredArgsConstructor
@RequestMapping(value = "/api/auth")
@Tag(name = "인증 API", description = "로그인 및 인증 관련 API")
public class AuthController {
    private final MemberService memberService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "유저 로그인 API")
    public ResponseEntity<ApiResponse<?>> login(@RequestBody @Valid MemberLoginRequest request, HttpServletRequest servletRequest) {
        LoginResponse response = memberService.login(request, servletRequest);
        JwtTokenDTO token = response.getJwtTokenDTO();
        ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", token.getAccessToken())
                .httpOnly(true)
                .secure(true)        // 로컬은 false, 배포 시 true
                .path("/")
                .maxAge(Duration.ofMillis(JwtTokenProvider.ACCESS_TOKEN_EXPIRY))
                .sameSite("None")
                .build();

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", token.getRefreshToken())
                .httpOnly(true)
                .secure(true)        // 로컬은 false, 배포 시 true
                .path("/")
                .maxAge(Duration.ofMillis(JwtTokenProvider.REFRESH_TOKEN_EXPIRY))
                .sameSite("None")
                .build();

        response.setJwtTokenDTO(null);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString(), refreshTokenCookie.toString())
                .body(ApiResponse.success(response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@CookieValue(name = "refreshToken") String refreshToken) {
        ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build();

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build();
        refreshTokenService.logout(refreshToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString(), refreshTokenCookie.toString())
                .body(ApiResponse.success(null));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<Void>> refreshToken(@CookieValue(name = "refreshToken") String refreshToken) {
        JwtTokenDTO token = refreshTokenService.refreshToken(refreshToken);
        ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", token.getAccessToken())
                .httpOnly(true)
                .secure(true)        // 로컬은 false, 배포 시 true
                .path("/")
                .maxAge(Duration.ofMillis(JwtTokenProvider.ACCESS_TOKEN_EXPIRY))
                .sameSite("None")
                .build();

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", token.getRefreshToken())
                .httpOnly(true)
                .secure(true)        // 로컬은 false, 배포 시 true
                .path("/")
                .maxAge(Duration.ofMillis(JwtTokenProvider.REFRESH_TOKEN_EXPIRY))
                .sameSite("None")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString(), refreshTokenCookie.toString())
                .body(ApiResponse.success(null));
    }
}
