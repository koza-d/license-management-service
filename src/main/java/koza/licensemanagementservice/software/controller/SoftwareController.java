package koza.licensemanagementservice.software.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import koza.licensemanagementservice.global.common.ApiResponse;
import koza.licensemanagementservice.member.dto.CustomUser;
import koza.licensemanagementservice.software.dto.SoftwareDTO;
import koza.licensemanagementservice.software.service.SoftwareService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/software")
@Tag(name = "소프트웨어 API", description = "소프트웨어 등록 및 관리 관련 API")
public class SoftwareController {
    private final SoftwareService softwareService;
    @Operation(summary = "소프트웨어 등록")
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createSoftware(@AuthenticationPrincipal CustomUser user,
                                                         @RequestBody @Valid SoftwareDTO.CreateRequest request) {
        SoftwareDTO.CreateResponse createResponse = softwareService.createSoftware(user, request);
        ApiResponse<?> response = ApiResponse.success(createResponse);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "소프트웨어 상세조회")
    @GetMapping("/{softwareId}")
    public ResponseEntity<ApiResponse<?>> getSoftware(@AuthenticationPrincipal CustomUser user, @PathVariable("softwareId") Long id) {
        SoftwareDTO.DetailResponse detailResponse = softwareService.getSoftwareDetail(user, id);
        ApiResponse<?> response = ApiResponse.success(detailResponse);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "소프트웨어 목록 조회", description = "로그인 유저의 소프트웨어 목록 조회")
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getSoftwareByMe(@AuthenticationPrincipal CustomUser user, Pageable pageable) {
        Page<SoftwareDTO.SummaryResponse> summaryResponses = softwareService.getSoftwareSummaryByMe(user, pageable);
        ApiResponse<?> response = ApiResponse.success(summaryResponses);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "소프트웨어 수정", description = "name, version 만 수정 가능")
    @PatchMapping("/{softwareId}")
    public ResponseEntity<ApiResponse<?>> updateSoftware(@AuthenticationPrincipal CustomUser user,
                                                         @PathVariable("softwareId") Long id,
                                                         @RequestBody @Valid SoftwareDTO.UpdateRequest request) {
        Long softwareId = softwareService.updateSoftware(user, id, request);
        ApiResponse<Long> response = ApiResponse.success(softwareId);
        return ResponseEntity.ok(response);
    }

}
