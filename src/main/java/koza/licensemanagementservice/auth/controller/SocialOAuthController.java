package koza.licensemanagementservice.auth.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import koza.licensemanagementservice.auth.dto.JwtTokenDTO;
import koza.licensemanagementservice.auth.dto.response.LoginResponse;
import koza.licensemanagementservice.auth.jwt.JwtTokenProvider;
import koza.licensemanagementservice.auth.service.OAuthService;
import koza.licensemanagementservice.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@Controller
@RequestMapping("/api/oauth")
@RequiredArgsConstructor
@Tag(name = "OAuth 인증 API", description = "소셜 로그인 및 인증 관련 API")
public class SocialOAuthController {
    private final OAuthService oAuthService;

    @GetMapping("/{provider}")
    public ResponseEntity<ApiResponse<?>> getLoginURL(@PathVariable("provider") String provider) {
        ApiResponse<String> response = ApiResponse.success(oAuthService.getAuthURL(provider));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{provider}/callback")
    public ResponseEntity<ApiResponse<?>> socialCallback(@RequestBody Map<String, String> body, @PathVariable("provider") String provider, HttpServletRequest httpRequest) {
        String authCode = body.get("code");
        LoginResponse response = oAuthService.login(provider, authCode, httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"));
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

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString(), refreshTokenCookie.toString())
                .body(ApiResponse.success(response));
    }

}
