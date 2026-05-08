package koza.licensemanagementservice.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import koza.licensemanagementservice.auth.dto.JwtTokenDTO;
import koza.licensemanagementservice.auth.dto.request.TokenRequest;
import koza.licensemanagementservice.auth.dto.response.LoginResponse;
import koza.licensemanagementservice.auth.dto.request.MemberLoginRequest;
import koza.licensemanagementservice.auth.service.RefreshTokenService;
import koza.licensemanagementservice.global.common.ApiResponse;
import koza.licensemanagementservice.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/auth")
@Tag(name = "인증 API", description = "로그인 및 인증 관련 API")
public class AuthController {
    private final MemberService memberService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "유저 로그인 API")
    public ResponseEntity<ApiResponse<?>> login(@RequestBody @Valid MemberLoginRequest request, HttpServletRequest servletRequest) {
        LoginResponse loginResponse = memberService.login(request, servletRequest);
        ApiResponse<?> response = ApiResponse.success(loginResponse);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "유저 로그아웃 API")
    public ResponseEntity<ApiResponse<?>> logout(@RequestBody @Valid TokenRequest request) {
        refreshTokenService.logout(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @PostMapping("/refresh")
    @Operation(summary = "토큰 갱신", description = "리프레시 토큰으로 액세스 토큰 재발급")
    public ResponseEntity<ApiResponse<?>> refreshToken(@RequestBody @Valid TokenRequest request) {
        JwtTokenDTO token = refreshTokenService.refreshToken(request.getRefreshToken());
        ApiResponse<JwtTokenDTO> response = ApiResponse.success(token);
        return ResponseEntity.ok(response);
    }
}
