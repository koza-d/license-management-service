package koza.licensemanagementservice.software.version.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import koza.licensemanagementservice.global.common.ApiResponse;
import koza.licensemanagementservice.auth.dto.CustomUser;
import koza.licensemanagementservice.software.version.dto.*;
import koza.licensemanagementservice.software.version.service.SoftwareVersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/software-versions")
@Tag(name = "소프트웨어 버전 관리 API", description = "소프트웨어 버전 관리 API")
public class SoftwareVersionController {

    private final SoftwareVersionService versionService;

    @Operation(summary = "소프트웨어 버전 등록")
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createVersion(@AuthenticationPrincipal CustomUser user,
                                                        @RequestBody @Valid SoftwareVersionCreateRequest request) {
        versionService.createVersion(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("create!!"));
    }

    @Operation(summary = "소프트웨어 버전 상세조회")
    @GetMapping("/{versionId}")
    public ResponseEntity<ApiResponse<?>> getVersion(@AuthenticationPrincipal CustomUser user,
                                                        @PathVariable("versionId") Long versionId) {
        SoftwareVersionDetailResponse detailResponse = versionService.getVersion(user, versionId);
        ApiResponse<SoftwareVersionDetailResponse> response = ApiResponse.success(detailResponse);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "소프트웨어 버전 목록조회")
    @GetMapping("/software/{softwareId}")
    public ResponseEntity<ApiResponse<?>> getVersionsBySoftware(@AuthenticationPrincipal CustomUser user,
                                                                @PathVariable("softwareId") Long softwareId) {
        List<SoftwareVersionSummaryResponse> versions = versionService.getVersions(user, softwareId);
        ApiResponse<List<SoftwareVersionSummaryResponse>> response = ApiResponse.success(versions);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "소프트웨어 버전 등록")
    @PatchMapping("/{versionId}")
    public ResponseEntity<ApiResponse<?>> updateVersion(@AuthenticationPrincipal CustomUser user,
                                                        @PathVariable("versionId") Long versionId,
                                                        @RequestBody @Valid SoftwareVersionUpdateRequest request) {
        versionService.updateVersion(user, versionId, request);
        return ResponseEntity.ok(ApiResponse.success("update!!"));
    }

}
