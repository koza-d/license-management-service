package koza.licensemanagementservice.verification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import koza.licensemanagementservice.global.common.ApiResponse;
import koza.licensemanagementservice.verification.dto.request.HeartbeatRequest;
import koza.licensemanagementservice.verification.dto.request.ReleaseRequest;
import koza.licensemanagementservice.verification.dto.request.VerifyRequest;
import koza.licensemanagementservice.verification.dto.resposne.HeartbeatResponse;
import koza.licensemanagementservice.verification.dto.resposne.VerifyResponse;
import koza.licensemanagementservice.verification.service.VerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/verification")
@Tag(name = "인증 API", description = "클라이언트에서 비로그인 상태로 사용하는 라이센스 인증 관련 API")
public class VerificationController {
    private final VerificationService verificationService;

    @Operation(summary = "라이센스 인증", description = "세션이 생성되는 기능이 있으며, 프로그램 최초 실행 시 호출하는 라이센스 인증 API")
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<?>> verify(@RequestBody VerifyRequest request) {
        VerifyResponse verifyResponse = verificationService.verify(request);
        ApiResponse<VerifyResponse> response = ApiResponse.success(verifyResponse);
        return ResponseEntity.ok(response);
    }
}
