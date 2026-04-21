package koza.licensemanagementservice.domain.session.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import koza.licensemanagementservice.auth.dto.CustomUser;
import koza.licensemanagementservice.domain.session.dto.request.SessionSearchCondition;
import koza.licensemanagementservice.domain.session.dto.request.SessionTerminateRequest;
import koza.licensemanagementservice.domain.session.dto.request.SessionTerminationsBulkRequest;
import koza.licensemanagementservice.domain.session.dto.response.SessionAdminDetailResponse;
import koza.licensemanagementservice.domain.session.dto.response.SessionAdminListResponse;
import koza.licensemanagementservice.domain.session.dto.response.SessionBulkTerminationResponse;
import koza.licensemanagementservice.domain.session.service.SessionAdminService;
import koza.licensemanagementservice.global.common.ApiResponse;
import koza.licensemanagementservice.global.common.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/admin/sessions")
@Tag(name = "세션 관리자용 API", description = "관리자용 세션 조회/종료 API")
public class SessionAdminController {
    private final SessionAdminService sessionAdminService;

    @Operation(summary = "관리자 전체 세션 목록", description = "활성 세션 목록 조회 (필터/검색/페이지네이션)")
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getSessions(@ModelAttribute SessionSearchCondition condition,
                                                      @PageableDefault(sort = "startedAt", direction = Sort.Direction.DESC)
                                                      Pageable pageable) {
        Page<SessionAdminListResponse> page = sessionAdminService.getSessions(condition, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page)));
    }

    @Operation(summary = "관리자 세션 상세", description = "userAgent 포함 상세 정보 반환")
    @GetMapping("/{sessionId}")
    public ResponseEntity<ApiResponse<?>> getSession(@PathVariable("sessionId") String sessionId) {
        SessionAdminDetailResponse detail = sessionAdminService.getSession(sessionId);
        return ResponseEntity.ok(ApiResponse.success(detail));
    }

    @Operation(summary = "세션 강제 종료", description = "관리자가 단일 세션을 종료")
    @PostMapping("/{sessionId}/terminations")
    public ResponseEntity<ApiResponse<?>> terminate(@AuthenticationPrincipal CustomUser user,
                                                    @PathVariable("sessionId") String sessionId,
                                                    @RequestBody(required = false) @Valid SessionTerminateRequest request) {
        sessionAdminService.terminate(user, sessionId, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "세션 일괄 강제 종료", description = "관리자가 여러 세션을 한 번에 종료")
    @PostMapping("/terminations")
    public ResponseEntity<ApiResponse<?>> terminateBulk(@AuthenticationPrincipal CustomUser user,
                                                        @RequestBody @Valid SessionTerminationsBulkRequest request) {
        SessionBulkTerminationResponse result = sessionAdminService.terminateBulk(user, request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
