package koza.licensemanagementservice.domain.session.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import koza.licensemanagementservice.domain.session.dto.request.TerminateBulkRequest;
import koza.licensemanagementservice.domain.session.dto.response.SessionResponse;
import koza.licensemanagementservice.global.common.ApiResponse;
import koza.licensemanagementservice.auth.dto.user.CustomUser;
import koza.licensemanagementservice.domain.session.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/sessions")
@Tag(name = "세션 API", description = "세션 관련 API")
public class SessionController {
    private final SessionService sessionService;

    @Operation(description = "접속중인 회원이 보유한 소프트웨어로 접속중인 세션 목록")
    @GetMapping("")
    public ResponseEntity<ApiResponse<?>> getAllByMember(@AuthenticationPrincipal CustomUser user,
                                                         @PageableDefault Pageable pageable) {
        Page<SessionResponse> sessions = sessionService.getAllByMember(user, pageable);
        ApiResponse<?> response = ApiResponse.success(sessions);
        return ResponseEntity.ok(response);
    }

    @Operation(description = "소프트웨어에 접속중인 세션 목록")
    @GetMapping("/software/{softwareId}")
    public ResponseEntity<ApiResponse<?>> getBySoftware(@AuthenticationPrincipal CustomUser user,
                                                        @PathVariable("softwareId") Long softwareId,
                                                        @PageableDefault Pageable pageable) {
        Page<SessionResponse> sessions = sessionService.getBySoftware(user, softwareId, pageable);
        ApiResponse<?> response = ApiResponse.success(sessions);
        return ResponseEntity.ok(response);
    }

    @Operation(description = "세션 강제종료")
    @PostMapping("/{sessionId}/terminate")
    public ResponseEntity<ApiResponse<?>> terminate(@AuthenticationPrincipal CustomUser user,
                                                    @PathVariable("sessionId") String sessionId) {
        sessionService.terminate(user, sessionId);
        ApiResponse<?> response = ApiResponse.success("terminate!");
        return ResponseEntity.ok(response);
    }

    @Operation(description = "세션 다수 강제종료")
    @PostMapping("/terminate/bulk")
    public ResponseEntity<ApiResponse<?>> terminateBulk(@AuthenticationPrincipal CustomUser user,
                                                        @RequestBody TerminateBulkRequest request) {
        sessionService.terminateBulk(user, request);
        ApiResponse<?> response = ApiResponse.success("terminate!");
        return ResponseEntity.ok(response);
    }

}
