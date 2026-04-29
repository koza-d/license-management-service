package koza.licensemanagementservice.domain.session.log.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import koza.licensemanagementservice.domain.session.log.dto.response.DailyUsageResponse;
import koza.licensemanagementservice.domain.session.log.dto.response.SessionHistoryResponse;
import koza.licensemanagementservice.domain.session.log.service.SessionLogService;
import koza.licensemanagementservice.global.common.ApiResponse;
import koza.licensemanagementservice.auth.dto.user.CustomUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/sessions-logs")
@Tag(name = "세션 로그 API", description = "세션 로그 통계 관련 API")
public class SessionLogController {
    private final SessionLogService logService;

    @Operation(description = "특정 라이센스 일일 사용시간 로그 API")
    @GetMapping("/licenses/{licenseId}/stats")
    public ResponseEntity<ApiResponse<?>> getDailyUsageTime(@AuthenticationPrincipal CustomUser user,
                                                             @PathVariable(name = "licenseId") Long licenseId,
                                                             @RequestParam(name = "range") int range) {
        List<DailyUsageResponse> responses = logService.getDailyUsageTime(user, licenseId, range);
        ApiResponse<List<DailyUsageResponse>> response = ApiResponse.success(responses);
        return ResponseEntity.ok(response);
    }

    @Operation(description = "특정 라이센스 사용 기록")
    @GetMapping("/licenses/{licenseId}")
    public ResponseEntity<ApiResponse<?>> getLicenseUsageHistory(@AuthenticationPrincipal CustomUser user,
                                                                 @PathVariable(name = "licenseId") Long licenseId,
                                                                 @PageableDefault(sort = "createAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<SessionHistoryResponse> responses = logService.getLicenseUsageHistory(user, licenseId, pageable);
        ApiResponse<?> response = ApiResponse.success(responses);
        return ResponseEntity.ok(response);
    }
}
