package koza.licensemanagementservice.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import koza.licensemanagementservice.auth.dto.MemberLoginRequest;
import koza.licensemanagementservice.global.common.ApiResponse;
import koza.licensemanagementservice.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
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

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "유저 로그인 API")
    public ResponseEntity<ApiResponse<?>> login(@RequestBody @Valid MemberLoginRequest request) {
        String token = memberService.login(request);
        ResponseCookie cookie = ResponseCookie.from("accessToken", token)
                .httpOnly(true)
                .secure(false)        // 로컬은 false, 배포 시 true
                .path("/")
                .maxAge(Duration.ofHours(1))
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponse.success(null));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("token", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok(ApiResponse.success(null));
    }

}
