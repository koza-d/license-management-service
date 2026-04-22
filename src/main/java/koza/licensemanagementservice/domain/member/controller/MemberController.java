package koza.licensemanagementservice.domain.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import koza.licensemanagementservice.domain.member.dto.request.MemberWithdrawRequest;
import koza.licensemanagementservice.global.common.ApiResponse;
import koza.licensemanagementservice.auth.dto.CustomUser;
import koza.licensemanagementservice.domain.member.dto.MemberInfoResponse;
import koza.licensemanagementservice.domain.member.dto.MemberJoinRequest;
import koza.licensemanagementservice.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping(value = "/api/members")
@Tag(name = "회원 API", description = "회원 도메인 관련 API")
public class MemberController {
    private final MemberService memberService;

    @PostMapping("")
    @Operation(summary = "회원가입", description = "유저의 회원가입 API")
    public ResponseEntity<ApiResponse<?>> join(@RequestBody @Valid MemberJoinRequest request) {
        Long memberId = memberService.join(request);
        ApiResponse<Long> response = ApiResponse.success(memberId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    @Operation(summary = "로그인 정보", description = "로그인 된 정보 확인용 API")
    public ResponseEntity<ApiResponse<?>> info(@AuthenticationPrincipal CustomUser user) {
        MemberInfoResponse infoResponse = memberService.userInfo(user);
        ApiResponse<MemberInfoResponse> response = ApiResponse.success(infoResponse);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/me")
    @Operation(summary = "회원탈퇴", description = "회원 탈퇴 API, 논리적 삭제")
    public ResponseEntity<ApiResponse<?>> withdraw(@AuthenticationPrincipal CustomUser user, @RequestBody MemberWithdrawRequest request) {
        memberService.withdraw(user, request);
        ApiResponse<String> response = ApiResponse.success("success");
        return ResponseEntity.ok(response);
    }
}
