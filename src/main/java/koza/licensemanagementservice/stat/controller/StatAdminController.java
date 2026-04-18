package koza.licensemanagementservice.stat.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import koza.licensemanagementservice.auth.dto.CustomUser;
import koza.licensemanagementservice.global.common.ApiResponse;
import koza.licensemanagementservice.stat.dto.MemberPlanDistributionResponse;
import koza.licensemanagementservice.stat.dto.MemberTrendResponse;
import koza.licensemanagementservice.stat.dto.SoftwareRegisterTrendResponse;
import koza.licensemanagementservice.stat.dto.SoftwareUsageResponse;
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
}
