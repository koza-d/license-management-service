package koza.licensemanagementservice.domain.audit.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import koza.licensemanagementservice.domain.audit.dto.request.AuditSearchCondition;
import koza.licensemanagementservice.domain.audit.dto.response.AuditLogResponse;
import koza.licensemanagementservice.domain.audit.service.AuditAdminService;
import koza.licensemanagementservice.global.common.ApiResponse;
import koza.licensemanagementservice.global.common.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/audit")
@Tag(name = "관리자 감사 로그 API", description = "라이센스/회원/소프트웨어 전역 감사 로그 검색")
public class AuditAdminController {
    private final AuditAdminService auditAdminService;

    @Operation(summary = "감사 로그 검색", description = "카테고리/작성자/기간/검색어 필터 + 페이지네이션")
    @GetMapping
    public ResponseEntity<ApiResponse<?>> search(
            @ModelAttribute AuditSearchCondition condition,
            Pageable pageable) {
        Page<AuditLogResponse> page = auditAdminService.search(condition, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page)));
    }
}
