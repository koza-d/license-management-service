package koza.licensemanagementservice.domain.software.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import koza.licensemanagementservice.auth.dto.CustomUser;
import koza.licensemanagementservice.domain.license.dto.response.AdminLicenseStatResponse;
import koza.licensemanagementservice.domain.software.dto.request.SoftwareBanRequest;
import koza.licensemanagementservice.domain.software.dto.request.SoftwareUnbanRequest;
import koza.licensemanagementservice.domain.software.dto.response.SoftwareAdminDetailResponse;
import koza.licensemanagementservice.domain.software.dto.response.SoftwareAdminStatsResponse;
import koza.licensemanagementservice.domain.software.dto.response.SoftwareAdminSummaryResponse;
import koza.licensemanagementservice.domain.software.log.dto.SoftwareLogResponse;
import koza.licensemanagementservice.domain.software.log.repository.SoftwareLogSearchCondition;
import koza.licensemanagementservice.domain.software.log.service.SoftwareAdminLogService;
import koza.licensemanagementservice.domain.software.repository.SoftwareAdminSearchCondition;
import koza.licensemanagementservice.domain.software.service.SoftwareAdminService;
import koza.licensemanagementservice.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/software")
@Tag(name = "[Admin] 소프트웨어 관리 API", description = "관리자 전용 소프트웨어 관리 API")
public class SoftwareAdminController {
    private final SoftwareAdminService softwareAdminService;
    private final SoftwareAdminLogService softwareAdminLogService;

    @Operation(summary = "소프트웨어 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getSoftwareList(@AuthenticationPrincipal CustomUser user,
                                                          @ModelAttribute SoftwareAdminSearchCondition condition,
                                                          @PageableDefault(sort = "createAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<SoftwareAdminSummaryResponse> list = softwareAdminService.getSoftwareList(user, condition, pageable);
        ApiResponse<?> response = ApiResponse.success(list);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "소프트웨어 상세 조회")
    @GetMapping("/{softwareId}")
    public ResponseEntity<ApiResponse<?>> getSoftwareDetail(@AuthenticationPrincipal CustomUser user,
                                                          @PathVariable("softwareId") Long softwareId) {
        SoftwareAdminDetailResponse detailResponse = softwareAdminService.getSoftwareDetail(user, softwareId);
        ApiResponse<?> response = ApiResponse.success(detailResponse);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "소프트웨어 집계 수치")
    @GetMapping("/{softwareId}/usage")
    public ResponseEntity<ApiResponse<?>> getSoftwareStats(@AuthenticationPrincipal CustomUser user,
                                                           @PathVariable("softwareId") Long softwareId) {
        SoftwareAdminStatsResponse detailResponse = softwareAdminService.getSoftwareStats(user, softwareId);
        ApiResponse<?> response = ApiResponse.success(detailResponse);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "소프트웨어 로그")
    @GetMapping("/{softwareId}/logs")
    public ResponseEntity<ApiResponse<?>> getSoftwareLogs(@AuthenticationPrincipal CustomUser user,
                                                          @PathVariable("softwareId") Long softwareId,
                                                          @ModelAttribute SoftwareLogSearchCondition condition,
                                                          @PageableDefault(sort = "createAt", direction = Sort.Direction.DESC) Pageable pageable
                                                          ) {
        Page<SoftwareLogResponse> detailResponse = softwareAdminLogService.getSoftwareLogs(user, softwareId, condition, pageable);
        ApiResponse<?> response = ApiResponse.success(detailResponse);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "소프트웨어 밴 처리", description = "소프트웨어의 상태를 밴으로 변경합니다.")
    @PostMapping("/{softwareId}/ban")
    public ResponseEntity<ApiResponse<?>> ban(@AuthenticationPrincipal CustomUser user,
                                                       @PathVariable Long softwareId,
                                                       @RequestBody @Valid SoftwareBanRequest request) {
        softwareAdminService.ban(user, softwareId, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "소프트웨어 밴 해제", description = "소프트웨어의 밴 상태를 해제합니다.")
    @PostMapping("/{softwareId}/unban")
    public ResponseEntity<ApiResponse<?>> unban(@AuthenticationPrincipal CustomUser user,
                                                @PathVariable Long softwareId,
                                                @RequestBody SoftwareUnbanRequest request) {
        softwareAdminService.unban(user, softwareId, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "소프트웨어별 라이센스 현황")
    @GetMapping("/software/{softwareId}/stats")
    public ResponseEntity<ApiResponse<?>> getLicenseStatBySoftware(@AuthenticationPrincipal CustomUser user,
                                                                   @PathVariable("softwareId") Long softwareId) {
        AdminLicenseStatResponse stat = softwareAdminService.getLicenseStat(user, softwareId);
        ApiResponse<?> response= ApiResponse.success(stat);
        return ResponseEntity.ok(response);
    }


//    @Operation(summary = "소프트웨어 상태 변경", description = "소프트웨어의 상태를 활성/정지로 변경합니다.")
//    @PatchMapping("/{softwareId}/status")
//    public ResponseEntity<ApiResponse<?>> changeStatus(@AuthenticationPrincipal CustomUser admin,
//                                                       @PathVariable Long softwareId,
//                                                       @RequestBody @Valid SoftwareStatusChangeRequest request) {
//        softwareAdminService.changeStatus(admin, softwareId, request);
//        return ResponseEntity.ok(ApiResponse.success(null));
//    }
}
