package koza.licensemanagementservice.domain.faq.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import koza.licensemanagementservice.auth.dto.user.CustomUser;
import koza.licensemanagementservice.domain.faq.dto.request.FaqCreateRequest;
import koza.licensemanagementservice.domain.faq.dto.request.FaqUpdateRequest;
import koza.licensemanagementservice.domain.faq.dto.response.FaqResponse;
import koza.licensemanagementservice.domain.faq.service.FaqService;
import koza.licensemanagementservice.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "FAQ API", description = "소프트웨어별 FAQ 관리 API")
public class FaqController {
    private final FaqService faqService;

    @Operation(summary = "FAQ 목록 조회", description = "소프트웨어별 FAQ 목록 (공개)")
    @GetMapping("/api/software/{softwareId}/faqs")
    public ResponseEntity<ApiResponse<?>> getFaqs(@PathVariable Long softwareId) {
        List<FaqResponse> faqs = faqService.getFaqsBySoftware(softwareId);
        return ResponseEntity.ok(ApiResponse.success(faqs));
    }

    @Operation(summary = "FAQ 등록", description = "소프트웨어 소유자만 등록 가능")
    @PostMapping("/api/software/{softwareId}/faqs")
    public ResponseEntity<ApiResponse<?>> createFaq(@AuthenticationPrincipal CustomUser user,
                                                    @PathVariable Long softwareId,
                                                    @RequestBody @Valid FaqCreateRequest request) {
        FaqResponse faq = faqService.createFaq(user, softwareId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(faq));
    }

    @Operation(summary = "FAQ 수정", description = "소프트웨어 소유자만 수정 가능")
    @PatchMapping("/api/faqs/{faqId}")
    public ResponseEntity<ApiResponse<?>> updateFaq(@AuthenticationPrincipal CustomUser user,
                                                    @PathVariable Long faqId,
                                                    @RequestBody FaqUpdateRequest request) {
        FaqResponse faq = faqService.updateFaq(user, faqId, request);
        return ResponseEntity.ok(ApiResponse.success(faq));
    }

    @Operation(summary = "FAQ 삭제", description = "소프트웨어 소유자만 삭제 가능")
    @DeleteMapping("/api/faqs/{faqId}")
    public ResponseEntity<ApiResponse<?>> deleteFaq(@AuthenticationPrincipal CustomUser user,
                                                    @PathVariable Long faqId) {
        faqService.deleteFaq(user, faqId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
