package koza.licensemanagementservice.domain.license.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import koza.licensemanagementservice.auth.dto.user.CustomUser;
import koza.licensemanagementservice.domain.license.dto.request.AdminLicenseExtendRequest;
import koza.licensemanagementservice.domain.license.dto.request.LicenseStatusUpdateRequest;
import koza.licensemanagementservice.domain.license.dto.response.AdminLicenseDetailResponse;
import koza.licensemanagementservice.domain.license.dto.response.AdminLicenseExtendResponse;
import koza.licensemanagementservice.domain.license.dto.response.AdminLicenseSummaryResponse;
import koza.licensemanagementservice.domain.license.log.dto.response.LicenseExtendLogResponse;
import koza.licensemanagementservice.domain.license.log.dto.response.LicenseLogResponse;
import koza.licensemanagementservice.domain.license.log.dto.condition.LicenseLogSearchCondition;
import koza.licensemanagementservice.domain.license.log.service.LicenseLogAdminService;
import koza.licensemanagementservice.domain.license.dto.condition.LicenseSearchCondition;
import koza.licensemanagementservice.domain.license.service.LicenseAdminService;
import koza.licensemanagementservice.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/admin/licenses")
@Tag(name = "라이센스 관리자용 API", description = "라이센스 관련 관리자용 API")
public class LicenseAdminController {
    private final LicenseAdminService licenseAdminService;
    private final LicenseLogAdminService licenseLogAdminService;

    @Operation(summary = "전체 라이센스 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getLicenseSummaryAll(@AuthenticationPrincipal CustomUser user,
                                                               @ModelAttribute LicenseSearchCondition condition,
                                                               Pageable pageable) {
        Page<AdminLicenseSummaryResponse> summaryResponses = licenseAdminService.getLicenseSummaryAll(user, condition, pageable);
        ApiResponse<?> response= ApiResponse.success(summaryResponses);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "라이센스 상세조회")
    @GetMapping("/{licenseId}")
    public ResponseEntity<ApiResponse<?>> getLicenseDetail(@AuthenticationPrincipal CustomUser user,
                                                           @PathVariable("licenseId") Long licenseId) {
        AdminLicenseDetailResponse detailResponse = licenseAdminService.getLicenseDetail(user, licenseId);
        ApiResponse<AdminLicenseDetailResponse> response = ApiResponse.success(detailResponse);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "라이센스 연장")
    @PostMapping("/{licenseId}/extend")
    public ResponseEntity<ApiResponse<?>> extendLicense(@AuthenticationPrincipal CustomUser user,
                                                       @PathVariable("licenseId") Long licenseId,
                                                       @RequestBody AdminLicenseExtendRequest request) {
        AdminLicenseExtendResponse extendResponse = licenseAdminService.extend(user, licenseId, request);
        ApiResponse<?> response = ApiResponse.success(extendResponse);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "라이센스 상태 변경", description = "라이센스 상태 변경 API")
    @PatchMapping("/{licenseId}/status")
    public ResponseEntity<ApiResponse<?>> changeStatus(@AuthenticationPrincipal CustomUser user,
                                                       @PathVariable("licenseId") Long licenseId,
                                                       @RequestBody LicenseStatusUpdateRequest request) {
        licenseAdminService.changeStatus(user, licenseId, request);
        ApiResponse<?> response = ApiResponse.success("success");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "연장 로그 목록 조회")
    @GetMapping("/{licenseId}/logs/extend")
    public ResponseEntity<ApiResponse<?>> getLicenseExtendLogs(@AuthenticationPrincipal CustomUser user,
                                                               @PathVariable("licenseId") Long licenseId,
                                                               @RequestParam(value = "from", required = false) LocalDate from,
                                                               @RequestParam(value = "to", required = false) LocalDate to,
                                                               @PageableDefault(sort = "createAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<LicenseExtendLogResponse> logResponses = licenseLogAdminService.getLicenseExtendLogs(user, licenseId, from, to, pageable);
        ApiResponse<?> response= ApiResponse.success(logResponses);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "변경 로그 목록 조회")
    @GetMapping("/{licenseId}/logs/changes")
    public ResponseEntity<ApiResponse<?>> getLicenseExtendLogs(@AuthenticationPrincipal CustomUser user,
                                                               @PathVariable("licenseId") Long licenseId,
                                                               @ModelAttribute LicenseLogSearchCondition condition,
                                                               @PageableDefault(sort = "createAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<LicenseLogResponse> logResponses = licenseLogAdminService.getLicenseChangedLogs(user, licenseId, condition, pageable);
        ApiResponse<?> response= ApiResponse.success(logResponses);
        return ResponseEntity.ok(response);
    }

}
