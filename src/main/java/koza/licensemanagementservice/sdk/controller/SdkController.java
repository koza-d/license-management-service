package koza.licensemanagementservice.sdk.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import koza.licensemanagementservice.global.common.ApiResponse;
import koza.licensemanagementservice.sdk.dto.request.HeartbeatRequest;
import koza.licensemanagementservice.sdk.dto.request.ReleaseRequest;
import koza.licensemanagementservice.sdk.dto.request.VerifyRequest;
import koza.licensemanagementservice.sdk.dto.resposne.HeartbeatResponse;
import koza.licensemanagementservice.sdk.dto.resposne.VerifyResponse;
import koza.licensemanagementservice.sdk.service.SdkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/sdk")
@Tag(name = "인증 API", description = "클라이언트에서 비로그인 상태로 사용하는 라이센스 인증 관련 API")
public class SdkController {
    private final SdkService sdkService;

    @Operation(summary = "라이센스 인증", description = "세션이 생성되는 기능이 있으며, 프로그램 최초 실행 시 호출하는 라이센스 인증 API")
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<?>> verify(@RequestBody VerifyRequest request,
                                                 HttpServletRequest servletRequest) throws Exception {
        VerifyResponse verifyResponse = sdkService.verify(request, servletRequest);
        ApiResponse<VerifyResponse> response = ApiResponse.success(verifyResponse);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "라이센스 하트비트", description = "짧은 시간 내에 요청을 보내 세션이 살아있음을 알리는 API")
    @PostMapping("/hb")
    public ResponseEntity<ApiResponse<?>> heartBeat(@RequestBody HeartbeatRequest request) throws Exception {
        HeartbeatResponse heartbeatResponse = sdkService.heartbeat(request);
        ApiResponse<HeartbeatResponse> response = ApiResponse.success(heartbeatResponse);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "라이센스 연결 해제", description = "세션을 해제하는 기능이 있으며, 프로그램 종료 시 호출되는 API")
    @PostMapping("/release")
    public ResponseEntity<ApiResponse<?>> release(@RequestBody ReleaseRequest request) {
        sdkService.release(request);
        return ResponseEntity.ok(ApiResponse.success("success"));
    }

}
