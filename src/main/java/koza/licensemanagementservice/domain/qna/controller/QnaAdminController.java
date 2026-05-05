package koza.licensemanagementservice.domain.qna.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import koza.licensemanagementservice.auth.dto.user.CustomUser;
import koza.licensemanagementservice.domain.qna.dto.request.QnaAdminSearchCondition;
import koza.licensemanagementservice.domain.qna.dto.request.QnaAnswerRequest;
import koza.licensemanagementservice.domain.qna.dto.request.QnaPriorityUpdateRequest;
import koza.licensemanagementservice.domain.qna.dto.response.AdminQnaSummaryResponse;
import koza.licensemanagementservice.domain.qna.dto.response.QnaDetailResponse;
import koza.licensemanagementservice.domain.qna.service.QnaAdminService;
import koza.licensemanagementservice.domain.qna.service.QnaService;
import koza.licensemanagementservice.global.common.ApiResponse;
import koza.licensemanagementservice.global.common.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/qna")
@Tag(name = "QNA 관리자용 API", description = "관리자용 Q&A 조회/답변 API")
public class QnaAdminController {
    private final QnaAdminService qnaAdminService;
    private final QnaService qnaService;

    @Operation(summary = "관리자 문의 목록", description = "status/필드 검색/페이징")
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getQuestions(@AuthenticationPrincipal CustomUser admin,
                                                        @ModelAttribute QnaAdminSearchCondition condition,
                                                        @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
                                                        Pageable pageable) {
        Page<AdminQnaSummaryResponse> page = qnaAdminService.getQuestions(admin, condition, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(page)));
    }

    @Operation(summary = "관리자 문의 단건 조회")
    @GetMapping("/{qnaId}")
    public ResponseEntity<ApiResponse<?>> getQuestionDetail(@AuthenticationPrincipal CustomUser admin,
                                                             @PathVariable Long qnaId) {
        QnaDetailResponse detail = qnaAdminService.getQuestionDetail(admin, qnaId);
        return ResponseEntity.ok(ApiResponse.success(detail));
    }

    @Operation(summary = "답변 제출", description = "관리자가 문의에 답변 저장")
    @PostMapping("/{qnaId}/answer")
    public ResponseEntity<ApiResponse<?>> submitAnswer(@AuthenticationPrincipal CustomUser user,
                                                        @PathVariable Long qnaId,
                                                        @RequestBody @Valid QnaAnswerRequest request) {
        QnaDetailResponse detail = qnaService.submitAnswer(user, qnaId, request);
        return ResponseEntity.ok(ApiResponse.success(detail));
    }

    @Operation(summary = "긴급도 변경", description = "관리자가 문의 긴급도(URGENT/NORMAL)를 변경")
    @PatchMapping("/{qnaId}/priority")
    public ResponseEntity<ApiResponse<?>> changePriority(@AuthenticationPrincipal CustomUser user,
                                                          @PathVariable Long qnaId,
                                                          @RequestBody @Valid QnaPriorityUpdateRequest request) {
        QnaDetailResponse detail = qnaService.changePriority(user, qnaId, request);
        return ResponseEntity.ok(ApiResponse.success(detail));
    }
}
