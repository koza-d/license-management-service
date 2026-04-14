package koza.licensemanagementservice.domain.software.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import koza.licensemanagementservice.auth.dto.CustomUser;
import koza.licensemanagementservice.domain.software.log.dto.response.SoftwareStatusLogResponse;
import koza.licensemanagementservice.domain.software.dto.request.SoftwareStatusChangeRequest;
import koza.licensemanagementservice.domain.software.service.SoftwareAdminService;
import koza.licensemanagementservice.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
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

    @Operation(summary = "소프트웨어 상태 변경", description = "소프트웨어의 상태를 활성/정지로 변경합니다.")
    @PatchMapping("/{softwareId}/status")
    public ResponseEntity<ApiResponse<?>> changeStatus(@AuthenticationPrincipal CustomUser admin,
                                                       @PathVariable Long softwareId,
                                                       @RequestBody @Valid SoftwareStatusChangeRequest request) {
        softwareAdminService.changeStatus(admin, softwareId, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "소프트웨어 상태 변경 이력 조회")
    @GetMapping("/{softwareId}/status-logs")
    public ResponseEntity<ApiResponse<?>> getStatusLogs(@PathVariable Long softwareId) {
        List<SoftwareStatusLogResponse> logs = softwareAdminService.getStatusLogs(softwareId);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }
}
