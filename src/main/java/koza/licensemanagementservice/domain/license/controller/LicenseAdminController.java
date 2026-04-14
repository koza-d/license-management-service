package koza.licensemanagementservice.domain.license.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import koza.licensemanagementservice.auth.dto.CustomUser;
import koza.licensemanagementservice.domain.license.dto.request.LicenseAdminExtendRequest;
import koza.licensemanagementservice.domain.license.dto.request.LicenseStatusUpdateRequest;
import koza.licensemanagementservice.domain.license.dto.response.LicenseAdminDetailResponse;
import koza.licensemanagementservice.domain.license.dto.response.LicenseAdminExtendResponse;
import koza.licensemanagementservice.domain.license.dto.response.LicenseAdminSummaryResponse;
import koza.licensemanagementservice.domain.license.repository.condition.LicenseSearchCondition;
import koza.licensemanagementservice.domain.license.service.LicenseAdminService;
import koza.licensemanagementservice.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/admin/licenses")
@Tag(name = "라이센스 관리자용 API", description = "라이센스 관련 관리자용 API")
public class LicenseAdminController {
    private final LicenseAdminService licenseAdminService;

    @Operation(summary = "전체 라이센스 목록 조회")
    @GetMapping("")
    public ResponseEntity<ApiResponse<?>> getLicenseSummaryAll(@AuthenticationPrincipal CustomUser user,
                                                               @ModelAttribute LicenseSearchCondition condition,
                                                               Pageable pageable) {
        Page<LicenseAdminSummaryResponse> summaryResponses = licenseAdminService.getLicenseSummaryAll(user, condition, pageable);
        ApiResponse<?> response= ApiResponse.success(summaryResponses);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "라이센스 상세조회")
    @GetMapping("/{licenseId}")
    public ResponseEntity<ApiResponse<?>> getLicenseDetail(@AuthenticationPrincipal CustomUser user,
                                                           @PathVariable("licenseId") Long licenseId) {
        LicenseAdminDetailResponse detailResponse = licenseAdminService.getLicenseDetail(user, licenseId);
        ApiResponse<LicenseAdminDetailResponse> response = ApiResponse.success(detailResponse);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "라이센스 연장")
    @PostMapping("/{licenseId}/extend")
    public ResponseEntity<ApiResponse<?>> extendLicense(@AuthenticationPrincipal CustomUser user,
                                                       @PathVariable("licenseId") Long licenseId,
                                                       @RequestBody LicenseAdminExtendRequest request) {
        LicenseAdminExtendResponse extendResponse = licenseAdminService.extend(user, licenseId, request);
        ApiResponse<?> response = ApiResponse.success(extendResponse);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "라이센스 상태 변경", description = "라이센스 상태 변경 API")
    @PostMapping("/{licenseId}/status")
    public ResponseEntity<ApiResponse<?>> changeStatus(@AuthenticationPrincipal CustomUser user,
                                                       @PathVariable("licenseId") Long licenseId,
                                                       @RequestBody LicenseStatusUpdateRequest request) {
        licenseAdminService.changeStatus(user, licenseId, request);
        ApiResponse<?> response = ApiResponse.success("success");
        return ResponseEntity.ok(response);
    }
}
