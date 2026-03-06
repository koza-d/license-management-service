package koza.licensemanagementservice.software.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import koza.licensemanagementservice.global.common.ApiResponse;
import koza.licensemanagementservice.member.dto.CustomUser;
import koza.licensemanagementservice.software.dto.SoftwareVersionDTO;
import koza.licensemanagementservice.software.service.SoftwareVersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/software-versions")
@Tag(name = "소프트웨어 버전 관리 API", description = "소프트웨어 버전 관리 API")
public class SoftwareVersionController {

    private final SoftwareVersionService versionService;

    @Operation(summary = "소프트웨어 버전 등록")
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createVersion(@AuthenticationPrincipal CustomUser user,
                                                        @RequestBody @Valid SoftwareVersionDTO.CreateRequest request) {
        versionService.createVersion(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("create!!"));
    }

    @Operation(summary = "소프트웨어 버전 등록")
    @PatchMapping("/{versionId}")
    public ResponseEntity<ApiResponse<?>> updateVersion(@AuthenticationPrincipal CustomUser user,
                                                        @PathVariable("versionId") Long versionId,
                                                        @RequestBody @Valid SoftwareVersionDTO.UpdateRequest request) {
        versionService.updateVersion(user, versionId, request);
        return ResponseEntity.ok(ApiResponse.success("update!!"));
    }

}
