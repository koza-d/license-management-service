package koza.licensemanagementservice.dashboard.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import koza.licensemanagementservice.dashboard.dto.DashboardStatsResponse;
import koza.licensemanagementservice.dashboard.dto.SoftwareStatsResponse;
import koza.licensemanagementservice.dashboard.service.DashboardService;
import koza.licensemanagementservice.global.common.ApiResponse;
import koza.licensemanagementservice.member.dto.CustomUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/dashboard")
@Tag(name = "대시보드 전용 API", description = "각종 대시보드에 필요한 데이터 조회용 API")
public class DashboardController {
    private final DashboardService dashboardService;

    @Operation(description = "대시보드 핵심 지표 API")
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<?>> getStats(@AuthenticationPrincipal CustomUser user) {
        DashboardStatsResponse stats = dashboardService.getStats(user);
        ApiResponse<DashboardStatsResponse> response = ApiResponse.success(stats);
        return ResponseEntity.ok(response);
    }

    @Operation(description = "대시보드 소프트웨어 사용 지표 API")
    @GetMapping("/software-stats")
    public ResponseEntity<ApiResponse<?>> getSoftwareStats(@AuthenticationPrincipal CustomUser user) {
        List<SoftwareStatsResponse> stats = dashboardService.getSoftwareStats(user);
        ApiResponse<?> response = ApiResponse.success(stats);
        return ResponseEntity.ok(response);
    }

}
