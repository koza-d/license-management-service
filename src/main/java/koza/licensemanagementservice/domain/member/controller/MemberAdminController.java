package koza.licensemanagementservice.domain.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import koza.licensemanagementservice.auth.dto.CustomUser;
import koza.licensemanagementservice.domain.log.dto.response.MemberGradeLogResponse;
import koza.licensemanagementservice.domain.log.dto.response.MemberStatusLogResponse;
import koza.licensemanagementservice.domain.member.dto.request.MemberGradeChangeRequest;
import koza.licensemanagementservice.domain.member.dto.request.MemberStatusChangeRequest;
import koza.licensemanagementservice.domain.member.service.MemberAdminService;
import koza.licensemanagementservice.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/members")
@Tag(name = "[Admin] 회원 관리 API", description = "관리자 전용 회원 관리 API")
public class MemberAdminController {
    private final MemberAdminService memberAdminService;

    @Operation(summary = "회원 상태 변경", description = "회원의 상태를 활성/정지로 변경합니다.")
    @PatchMapping("/{memberId}/status")
    public ResponseEntity<ApiResponse<?>> changeStatus(@AuthenticationPrincipal CustomUser admin,
                                                       @PathVariable Long memberId,
                                                       @RequestBody @Valid MemberStatusChangeRequest request) {
        memberAdminService.changeStatus(admin, memberId, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "회원 등급 변경", description = "회원의 등급을 변경합니다.")
    @PatchMapping("/{memberId}/grade")
    public ResponseEntity<ApiResponse<?>> changeGrade(@AuthenticationPrincipal CustomUser admin,
                                                      @PathVariable Long memberId,
                                                      @RequestBody @Valid MemberGradeChangeRequest request) {
        memberAdminService.changeGrade(admin, memberId, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(summary = "회원 상태 변경 이력 조회")
    @GetMapping("/{memberId}/status-logs")
    public ResponseEntity<ApiResponse<?>> getStatusLogs(@PathVariable Long memberId) {
        List<MemberStatusLogResponse> logs = memberAdminService.getStatusLogs(memberId);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    @Operation(summary = "회원 등급 변경 이력 조회")
    @GetMapping("/{memberId}/grade-logs")
    public ResponseEntity<ApiResponse<?>> getGradeLogs(@PathVariable Long memberId) {
        List<MemberGradeLogResponse> logs = memberAdminService.getGradeLogs(memberId);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }
}
