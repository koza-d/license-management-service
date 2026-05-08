package koza.licensemanagementservice.auth.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import koza.licensemanagementservice.auth.dto.response.LoginResponse;
import koza.licensemanagementservice.auth.service.OAuthService;
import koza.licensemanagementservice.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/oauth")
@RequiredArgsConstructor
@Tag(name = "OAuth 인증 API", description = "소셜 로그인 및 인증 관련 API")
public class SocialOAuthController {
    private final OAuthService oAuthService;

    @GetMapping("/{provider}")
    public ResponseEntity<ApiResponse<?>> getLoginURL(@PathVariable("provider") String provider) {
        return ResponseEntity.ok(ApiResponse.success(oAuthService.getAuthURL(provider)));
    }

    @PostMapping("/{provider}/callback")
    public ResponseEntity<ApiResponse<?>> socialCallback(@RequestBody Map<String, String> body, @PathVariable("provider") String provider, HttpServletRequest httpRequest) {
        String authCode = body.get("code");
        LoginResponse response = oAuthService.login(provider, authCode, httpRequest.getRemoteAddr(), httpRequest.getHeader("User-Agent"));
        return ResponseEntity.ok(ApiResponse.success(response));
    }

}
