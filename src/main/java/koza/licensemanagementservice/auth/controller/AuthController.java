package koza.licensemanagementservice.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import koza.licensemanagementservice.auth.dto.LoginRequest;
import koza.licensemanagementservice.global.common.ApiResponse;
import koza.licensemanagementservice.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping(value = "/api/auth")
@Tag(name = "인증 API", description = "로그인 및 인증 관련 API")
public class AuthController {

    private final MemberService memberService;

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "유저 로그인 API")
    public ResponseEntity<ApiResponse<?>> login(@RequestBody @Valid LoginRequest request) {
        String token = memberService.login(request);
        ApiResponse<String> response = ApiResponse.success(token);
        return ResponseEntity.ok(response);
    }

}
