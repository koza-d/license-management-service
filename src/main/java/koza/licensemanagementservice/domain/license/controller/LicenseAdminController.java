package koza.licensemanagementservice.domain.license.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import koza.licensemanagementservice.auth.dto.CustomUser;
import koza.licensemanagementservice.domain.license.dto.request.LicenseStatusUpdateRequest;
import koza.licensemanagementservice.domain.license.service.LicenseAdminService;
import koza.licensemanagementservice.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/admin/licenses")
@Tag(name = "라이센스 관리자용 API", description = "라이센스 관련 관리자용 API")
public class LicenseAdminController {
    private final LicenseAdminService licenseAdminService;

    @Operation(summary = "라이센스 상태 변경", description = "라이센스 상태 변경 API")
    @PatchMapping("/{licenseId}/status")
    public ResponseEntity<ApiResponse<?>> changeStatus(@AuthenticationPrincipal CustomUser user,
                                                       @PathVariable("licenseId") Long licenseId,
                                                       @RequestBody LicenseStatusUpdateRequest request) {
        licenseAdminService.changeStatus(user, licenseId, request);
        ApiResponse<?> response = ApiResponse.success("success");
        return ResponseEntity.ok(response);
    }
}
