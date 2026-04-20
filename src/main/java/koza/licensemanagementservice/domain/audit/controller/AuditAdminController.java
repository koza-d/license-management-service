package koza.licensemanagementservice.domain.audit.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import koza.licensemanagementservice.domain.audit.dto.request.AuditSearchCondition;
import koza.licensemanagementservice.domain.audit.dto.response.AuditLogResponse;
import koza.licensemanagementservice.domain.audit.entity.EventCategory;
import koza.licensemanagementservice.domain.audit.service.AuditAdminService;
import koza.licensemanagementservice.global.common.ApiResponse;
import koza.licensemanagementservice.global.common.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/audit")
@Tag(name = "관리자 감사 로그 API", description = "라이센스/회원/소프트웨어 전역 감사 로그 검색")
public class AuditAdminController {
    private final AuditAdminService auditAdminService;

    @Operation(summary = "감사 로그 검색", description = "카테고리/작성자/기간/검색어 필터 + 페이지네이션")
    @GetMapping
    public ResponseEntity<ApiResponse<?>> search(
            @RequestParam(required = false) Set<EventCategory> category,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String actorEmail,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) Long targetId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) String q,
            Pageable pageable) {
        AuditSearchCondition condition = AuditSearchCondition.builder()
                .category(category)
                .eventType(eventType)
                .actorEmail(actorEmail)
                .targetType(targetType)
                .targetId(targetId)
                .from(from)
                .to(to)
                .q(q)
                .build();
        Page<AuditLogResponse> page = auditAdminService.search(condition, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page)));
    }
}
