package koza.licensemanagementservice.domain.license.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import koza.licensemanagementservice.domain.license.dto.request.LicenseExtendRequest;
import koza.licensemanagementservice.domain.license.dto.request.LicenseIssueRequest;
import koza.licensemanagementservice.domain.license.dto.request.LicenseStatusUpdateRequest;
import koza.licensemanagementservice.domain.license.dto.request.LicenseUpdateRequest;
import koza.licensemanagementservice.domain.license.dto.response.LicenseDetailResponse;
import koza.licensemanagementservice.domain.license.dto.response.LicenseExtendResponse;
import koza.licensemanagementservice.domain.license.dto.response.LicenseIssueResponse;
import koza.licensemanagementservice.domain.license.dto.response.LicenseSummaryResponse;
import koza.licensemanagementservice.global.common.ApiResponse;
import koza.licensemanagementservice.domain.license.service.LicenseService;
import koza.licensemanagementservice.auth.dto.CustomUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/licenses")
@Tag(name = "라이센스 API", description = "라이센스 관련 API")
public class LicenseController {
    private final LicenseService licenseService;


    @Operation(summary = "라이센스 발급", description = "라이센스 발급 API")
    @PostMapping
    public ResponseEntity<ApiResponse<?>> issueLicense(@AuthenticationPrincipal CustomUser user
            , @RequestBody @Valid LicenseIssueRequest request) {
        LicenseIssueResponse issueResponse = licenseService.issueLicense(user, request);
        ApiResponse<LicenseIssueResponse> response = ApiResponse.success(issueResponse);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "라이센스 상세조회")
    @GetMapping("/{licenseId}")
    public ResponseEntity<ApiResponse<?>> getLicenseDetail(@AuthenticationPrincipal CustomUser user,
                                                           @PathVariable("licenseId") Long licenseId) {
        LicenseDetailResponse detailResponse = licenseService.getLicenseDetail(user, licenseId);
        ApiResponse<LicenseDetailResponse> response = ApiResponse.success(detailResponse);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "전체 라이센스 목록 조회")
    @GetMapping("")
    public ResponseEntity<ApiResponse<?>> getLicenseSummaryAll(@AuthenticationPrincipal CustomUser user,
                                                               @RequestParam(required = false, name = "search") String search,
                                                               @RequestParam(required = false, name = "hasActiveSession") Boolean hasActiveSession,
                                                               @RequestParam(required = false, name = "expireWithin") Integer expireWithin,
                                                               Pageable pageable) {
        Page<LicenseSummaryResponse> summaryResponses = licenseService.getLicenseSummaryAll(user, search, hasActiveSession, expireWithin, pageable);
        ApiResponse<?> response= ApiResponse.success(summaryResponses);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "소프트웨어 별 라이센스 목록 조회")
    @GetMapping("/software/{softwareId}")
    public ResponseEntity<ApiResponse<?>> getLicenseSummaryBySoftware(@AuthenticationPrincipal CustomUser user,
                                                                      @PathVariable("softwareId") Long softwareId,
                                                                      @RequestParam(required = false, name = "search") String search,
                                                                      @RequestParam(required = false, name = "hasActiveSession") Boolean hasActiveSession,
                                                                      Pageable pageable) {
        Page<LicenseSummaryResponse> summaryResponses = licenseService.getLicenseSummaryBySoftware(user, softwareId, search, hasActiveSession, pageable);
        ApiResponse<?> response= ApiResponse.success(summaryResponses);
        return ResponseEntity.ok(response);

    }

    @Operation(summary = "라이센스 연장", description = "라이센스 여러 개를 한 번에 연장하는 API")
    @PostMapping("/bulk-extend")
    public ResponseEntity<ApiResponse<?>> extendLicense(@AuthenticationPrincipal CustomUser user,
                                                        @RequestBody @Valid LicenseExtendRequest request) {
        List<LicenseExtendResponse> extendResponses = licenseService.extendLicense(user, request.getSoftwareId(), request);
        ApiResponse<?> response = ApiResponse.success(extendResponses);
        return ResponseEntity.ok(response);

    }

    @Operation(summary = "라이센스 연장 전 확인용", description = "선택한 연장할 라이센스 확인용 API")
    @GetMapping("/bulk-extend/preview")
    public ResponseEntity<ApiResponse<?>> getPreviewExtendLicense(@AuthenticationPrincipal CustomUser user,
                                                                        @RequestParam(name = "ids") String request) {
        List<Long> ids = Arrays.stream(request.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());
        List<LicenseSummaryResponse> summaryLicenses = licenseService.getPreviewExtendLicense(user, ids);
        ApiResponse<?> response = ApiResponse.success(summaryLicenses);
        return ResponseEntity.ok(response);

    }

    @Operation(summary = "라이센스 수정", description = "라이센스 이름, 비고, 메타데이터 등 수정 API")
    @PostMapping("/{licenseId}")
    public ResponseEntity<ApiResponse<?>> updateLicense(@AuthenticationPrincipal CustomUser user,
                                                        @PathVariable("licenseId") Long licenseId,
                                                        @RequestBody @Valid LicenseUpdateRequest request) {
        licenseService.updateLicense(user, licenseId, request);
        ApiResponse<?> response = ApiResponse.success("success");
        return ResponseEntity.ok(response);

    }

    @Operation(summary = "라이센스 상태 변경", description = "라이센스 상태 변경 API")
    @PatchMapping("/{licenseId}/status")
    public ResponseEntity<ApiResponse<?>> changeStatus(@AuthenticationPrincipal CustomUser user,
                                                       @PathVariable("licenseId") Long licenseId,
                                                       @RequestBody LicenseStatusUpdateRequest request) {

        licenseService.changeStatus(user, licenseId, request);
        ApiResponse<?> response = ApiResponse.success("success");
        return ResponseEntity.ok(response);
    }

}
