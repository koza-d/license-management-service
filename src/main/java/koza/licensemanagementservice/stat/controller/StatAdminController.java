package koza.licensemanagementservice.stat.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import koza.licensemanagementservice.auth.dto.user.CustomUser;
import koza.licensemanagementservice.global.common.ApiResponse;
import koza.licensemanagementservice.stat.dto.*;
import koza.licensemanagementservice.stat.dto.response.*;
import koza.licensemanagementservice.stat.service.StatAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/stats")
@Tag(name = "[Admin] 통계 API", description = "관리자 톻계 API")
public class StatAdminController {
    private final StatAdminService statAdminService;

    @GetMapping("/members/trends")
    @Operation(summary = "회원 수 추이", description = "날짜별 가입/탈퇴/순증 조회")
    public ResponseEntity<ApiResponse<?>> memberTrend(@AuthenticationPrincipal CustomUser user,
                                                      @RequestParam LocalDate from,
                                                      @RequestParam LocalDate to) {
        List<MemberTrendResponse> memberTrend = statAdminService.getMemberTrend(user, from, to);
        ApiResponse<?> response = ApiResponse.success(memberTrend);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/members/plan-distribution")
    @Operation(summary = "회원 플랜 분포", description = "현재 유저의 플랜 분포")
    public ResponseEntity<ApiResponse<?>> getMemberPlanDistribution(@AuthenticationPrincipal CustomUser user) {
        MemberPlanDistributionResponse planDistribution = statAdminService.getMemberPlanDistribution(user);
        ApiResponse<?> response = ApiResponse.success(planDistribution);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/software/registration-trends")
    @Operation(summary = "회원 플랜 분포", description = "현재 유저의 플랜 분포")
    public ResponseEntity<ApiResponse<?>> getSoftwareRegistrationTrends(@AuthenticationPrincipal CustomUser user,
                                                                       @RequestParam LocalDate from,
                                                                       @RequestParam LocalDate to) {
        List<SoftwareRegisterTrendResponse> registrationTrends = statAdminService.getSoftwareRegistrationTrends(user, from, to);
        ApiResponse<?> response = ApiResponse.success(registrationTrends);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/software/top-usage")
    @Operation(summary = "소프트웨어 top N 사용시간", description = "소프트웨어 하위 라이센스의 사용시간")
    public ResponseEntity<ApiResponse<?>> getSoftwareTopNUsage(@AuthenticationPrincipal CustomUser user,
                                                                        @RequestParam Integer topN) {
        List<SoftwareUsageResponse> topNUsage = statAdminService.getSoftwareTopNUsage(user, topN);
        ApiResponse<?> response = ApiResponse.success(topNUsage);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/licenses/status-trends")
    @Operation(summary = "라이센스 날짜별 상태 통계", description = "라이센스 발급, 만료, 정지 날짜별 통계")
    public ResponseEntity<ApiResponse<?>> getLicenseStatusTrends(@AuthenticationPrincipal CustomUser user,
                                                                 @RequestParam LocalDate from,
                                                                 @RequestParam LocalDate to) {
        List<LicenseStatusTrendResponse> licenseStatusTrendResponses = statAdminService.getLicenseStatusTrends(user, from, to);
        ApiResponse<?> response = ApiResponse.success(licenseStatusTrendResponses);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/verifications/metrics")
    @Operation(summary = "라이센스 인증 실패율 추이", description = "날짜별 라이센스 인증 실패율 통계, 이상치 감지")
    public ResponseEntity<ApiResponse<?>> getVerificationMetrics(@AuthenticationPrincipal CustomUser user,
                                                                 @RequestParam LocalDate from,
                                                                 @RequestParam LocalDate to) {
        List<VerificationAttemptTrend> verificationTrends = statAdminService.getVerificationMetrics(user, from, to);
        ApiResponse<?> response = ApiResponse.success(verificationTrends);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sessions/usage-pattern")
    @Operation(summary = "세션 피크 타임", description = "시간별, 요일별 세션 피크 패턴 조회")
    public ResponseEntity<ApiResponse<?>> getSessionUsagePattern(@AuthenticationPrincipal CustomUser user,
                                                                 @RequestParam LocalDate from,
                                                                 @RequestParam LocalDate to) {
        List<SessionPeakResponse> sessionPeakByDays = statAdminService.getSessionPeakByDays(user, from, to);
        List<SessionPeakResponse> sessionPeakByHours = statAdminService.getSessionPeakByHours(user, from, to);
        ApiResponse<?> response = ApiResponse.success(Map.of(
                "byDays", sessionPeakByDays,
                "byHours", sessionPeakByHours
        ));
        return ResponseEntity.ok(response);
    }
}
