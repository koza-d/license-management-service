package koza.licensemanagementservice.session.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import koza.licensemanagementservice.global.common.ApiResponse;
import koza.licensemanagementservice.member.dto.CustomUser;
import koza.licensemanagementservice.session.dto.SessionResponse;
import koza.licensemanagementservice.session.service.SessionService;
import koza.licensemanagementservice.verification.service.SessionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/sessions")
@Tag(name = "세션 API", description = "세션 관련 API")
public class SessionController {
    private final SessionService sessionService;

    @Operation(description = "접속중인 회원이 보유한 소프트웨어로 접속중인 세션 목록")
    @GetMapping("")
    public ResponseEntity<ApiResponse<?>> getAllByMember(@AuthenticationPrincipal CustomUser user) {
        List<SessionResponse> sessions = sessionService.getAllByMember(user);
        ApiResponse<?> response = ApiResponse.success(sessions);
        return ResponseEntity.ok(response);
    }
}
