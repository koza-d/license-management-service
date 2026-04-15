package koza.licensemanagementservice.domain.software.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import koza.licensemanagementservice.auth.dto.CustomUser;
import koza.licensemanagementservice.domain.software.dto.response.SoftwareAdminSummaryResponse;
import koza.licensemanagementservice.domain.software.dto.request.SoftwareStatusChangeRequest;
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

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/software")
@Tag(name = "[Admin] 소프트웨어 관리 API", description = "관리자 전용 소프트웨어 관리 API")
public class SoftwareAdminController {
    private final SoftwareAdminService softwareAdminService;

    @Operation(summary = "소프트웨어 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getSoftwareList(@AuthenticationPrincipal CustomUser user,
                                                          @ModelAttribute SoftwareAdminSearchCondition condition,
                                                          @PageableDefault(sort = "createAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<SoftwareAdminSummaryResponse> list = softwareAdminService.getSoftwareList(user, condition, pageable);
        ApiResponse<?> response = ApiResponse.success(list);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "소프트웨어 상태 변경", description = "소프트웨어의 상태를 활성/정지로 변경합니다.")
    @PostMapping("/{softwareId}/status")
    public ResponseEntity<ApiResponse<?>> changeStatus(@AuthenticationPrincipal CustomUser admin,
                                                       @PathVariable Long softwareId,
                                                       @RequestBody @Valid SoftwareStatusChangeRequest request) {
        softwareAdminService.changeStatus(admin, softwareId, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

}
