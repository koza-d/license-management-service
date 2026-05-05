package koza.licensemanagementservice.domain.session.log.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import koza.licensemanagementservice.auth.dto.user.CustomUser;
import koza.licensemanagementservice.domain.session.log.dto.response.SessionHistoryResponse;
import koza.licensemanagementservice.domain.session.log.dto.condition.SessionLogSearchCondition;
import koza.licensemanagementservice.domain.session.log.service.SessionLogAdminService;
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

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/admin/sessions-logs")
@Tag(name = "관리자용 세션 로그 API", description = "세션 로그 통계 관련 API")
public class SessionLogAdminController {
    private final SessionLogAdminService logAdminService;

    @Operation(description = "특정 라이센스 사용 기록")
    @GetMapping("/licenses/{licenseId}")
    public ResponseEntity<ApiResponse<?>> getLicenseUsageHistory(@AuthenticationPrincipal CustomUser user,
                                                                 @PathVariable(name = "licenseId") Long licenseId,
                                                                 @ModelAttribute SessionLogSearchCondition condition,
                                                                 @PageableDefault(sort = "createAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<SessionHistoryResponse> responses = logAdminService.getLicenseUsageHistory(user, licenseId, condition, pageable);
        ApiResponse<?> response = ApiResponse.success(responses);
        return ResponseEntity.ok(response);
    }
}
