package koza.licensemanagementservice.domain.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import koza.licensemanagementservice.auth.dto.CustomUser;
import koza.licensemanagementservice.domain.member.dto.response.AdminMemberDetailResponse;
import koza.licensemanagementservice.domain.member.dto.response.AdminMemberSummaryResponse;
import koza.licensemanagementservice.domain.member.dto.request.MemberGradeChangeRequest;
import koza.licensemanagementservice.domain.member.dto.request.MemberRoleChangeRequest;
import koza.licensemanagementservice.domain.member.dto.request.MemberStatusChangeRequest;
import koza.licensemanagementservice.domain.member.entity.MemberStatus;
import koza.licensemanagementservice.domain.member.log.dto.response.MemberLogResponse;
import koza.licensemanagementservice.domain.member.log.entity.MemberLogType;
import koza.licensemanagementservice.domain.member.service.MemberAdminService;
import koza.licensemanagementservice.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Operation(summary = "회원 목록 조회", description = "이메일/닉네임 키워드 검색 + 상태 필터 + 페이지네이션")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<AdminMemberSummaryResponse>>> getMembers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) MemberStatus status,
            Pageable pageable) {
        Page<AdminMemberSummaryResponse> members = memberAdminService.getMembers(keyword, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(members));
    }

    @Operation(summary = "회원 상세 조회")
    @GetMapping("/{memberId}")
    public ResponseEntity<ApiResponse<AdminMemberDetailResponse>> getMemberDetail(@PathVariable Long memberId) {
        AdminMemberDetailResponse detail = memberAdminService.getMemberDetail(memberId);
        return ResponseEntity.ok(ApiResponse.success(detail));
    }

    @Operation(summary = "회원 로그 조회", description = "type 생략 시 전체, 지정 시 해당 타입만 반환합니다.")
    @GetMapping("/{memberId}/logs")
    public ResponseEntity<ApiResponse<List<MemberLogResponse>>> getLogs(
            @PathVariable Long memberId,
            @RequestParam(required = false) MemberLogType type) {
        List<MemberLogResponse> logs = memberAdminService.getLogs(memberId, type);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

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

    @Operation(summary = "회원 역할 변경", description = "회원의 역할(USER/ADMIN)을 변경합니다. 본인 역할은 변경할 수 없습니다.")
    @PatchMapping("/{memberId}/role")
    public ResponseEntity<ApiResponse<?>> changeRole(@AuthenticationPrincipal CustomUser admin,
                                                     @PathVariable Long memberId,
                                                     @RequestBody @Valid MemberRoleChangeRequest request) {
        memberAdminService.changeRole(admin, memberId, request);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
