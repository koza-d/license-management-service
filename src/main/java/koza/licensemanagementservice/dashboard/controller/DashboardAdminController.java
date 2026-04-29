package koza.licensemanagementservice.dashboard.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import koza.licensemanagementservice.auth.dto.user.CustomUser;
import koza.licensemanagementservice.domain.audit.dto.response.AdminRecentAuditResponse;
import koza.licensemanagementservice.dashboard.dto.response.AdminStatsResponse;
import koza.licensemanagementservice.dashboard.dto.response.PendingQnaResponse;
import koza.licensemanagementservice.dashboard.service.DashboardAdminService;
import koza.licensemanagementservice.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/dashboard")
@Tag(name = "관리자 대시보드 API", description = "관리자 대시보드용 집계 데이터 조회 API")
public class DashboardAdminController {
    private final DashboardAdminService dashboardAdminService;

    @Operation(summary = "관리자 대시보드 핵심 지표", description = "전체 라이센스/세션/회원/문의 현황 집계")
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<?>> getStats(@AuthenticationPrincipal CustomUser admin) {
        AdminStatsResponse stats = dashboardAdminService.getStats(admin);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @Operation(summary = "개입 대기 문의 위젯", description = "PENDING 상태 상위 N건 (URGENT 우선, 오래된 문의 먼저)")
    @GetMapping("/pending-qna")
    public ResponseEntity<ApiResponse<?>> getPendingQna(
            @AuthenticationPrincipal CustomUser admin,
            @RequestParam(defaultValue = "5") int limit) {
        List<PendingQnaResponse> items = dashboardAdminService.getPendingQna(admin, limit);
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    @Operation(summary = "최근 감사 로그 위젯", description = "라이센스/회원/소프트웨어 전역 최근 이벤트 상위 N건")
    @GetMapping("/recent-audit")
    public ResponseEntity<ApiResponse<?>> getRecentAudit(
            @AuthenticationPrincipal CustomUser admin,
            @RequestParam(defaultValue = "20") int limit) {
        List<AdminRecentAuditResponse> items = dashboardAdminService.getRecentAudit(admin, limit);
        return ResponseEntity.ok(ApiResponse.success(items));
    }
}
