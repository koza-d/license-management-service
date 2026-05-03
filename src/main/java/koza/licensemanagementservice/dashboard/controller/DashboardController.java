package koza.licensemanagementservice.dashboard.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import koza.licensemanagementservice.auth.dto.user.CustomUser;
import koza.licensemanagementservice.dashboard.dto.response.ActiveSessionResponse;
import koza.licensemanagementservice.dashboard.dto.response.ExpiringLicenseResponse;
import koza.licensemanagementservice.dashboard.dto.response.LicenseStatsResponse;
import koza.licensemanagementservice.dashboard.dto.response.SoftwareUsageResponse;
import koza.licensemanagementservice.dashboard.dto.response.VendorStatsResponse;
import koza.licensemanagementservice.dashboard.service.DashboardService;
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
@RequestMapping("/api/dashboard")
@Tag(name = "대시보드 전용 API", description = "공급자(vendor) 대시보드 화면용 데이터 API")
public class DashboardController {
    private final DashboardService dashboardService;

    @Operation(description = "요약 통계 (KPI 카드용) — 등록 SW, 발급 라이센스, 활성 세션")
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<?>> getStats(@AuthenticationPrincipal CustomUser user) {
        VendorStatsResponse stats = dashboardService.getStats(user);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @Operation(description = "라이센스 현황 — 상태별/세션 사용 분포/만료 임박")
    @GetMapping("/licenses/stats")
    public ResponseEntity<ApiResponse<?>> getLicenseStats(@AuthenticationPrincipal CustomUser user) {
        LicenseStatsResponse stats = dashboardService.getLicenseStats(user);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @Operation(description = "만료 임박 라이센스 목록")
    @GetMapping("/licenses/expiring-soon")
    public ResponseEntity<ApiResponse<?>> getExpiringSoonLicenses(
            @AuthenticationPrincipal CustomUser user,
            @RequestParam(defaultValue = "10") int limit) {
        List<ExpiringLicenseResponse> licenses = dashboardService.getExpiringSoonLicenses(user, limit);
        return ResponseEntity.ok(ApiResponse.success(licenses));
    }

    @Operation(description = "현재 활성 세션 목록 (대시보드 미니뷰)")
    @GetMapping("/sessions/active")
    public ResponseEntity<ApiResponse<?>> getActiveSessions(
            @AuthenticationPrincipal CustomUser user,
            @RequestParam(defaultValue = "10") int limit) {
        List<ActiveSessionResponse> sessions = dashboardService.getActiveSessions(user, limit);
        return ResponseEntity.ok(ApiResponse.success(sessions));
    }

    @Operation(description = "소프트웨어별 사용량 TOP N (days=7 또는 30)")
    @GetMapping("/software-usage")
    public ResponseEntity<ApiResponse<?>> getSoftwareUsage(
            @AuthenticationPrincipal CustomUser user,
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "5") int limit) {
        List<SoftwareUsageResponse> usage = dashboardService.getSoftwareUsage(user, days, limit);
        return ResponseEntity.ok(ApiResponse.success(usage));
    }
}
