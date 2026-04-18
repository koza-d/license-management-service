package koza.licensemanagementservice.domain.qna.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import koza.licensemanagementservice.auth.dto.CustomUser;
import koza.licensemanagementservice.domain.qna.dto.request.QnaAnswerRequest;
import koza.licensemanagementservice.domain.qna.dto.request.QnaCreateRequest;
import koza.licensemanagementservice.domain.qna.dto.request.QnaPriorityUpdateRequest;
import koza.licensemanagementservice.domain.qna.dto.request.QnaStatusUpdateRequest;
import koza.licensemanagementservice.domain.qna.dto.response.QnaDetailResponse;
import koza.licensemanagementservice.domain.qna.dto.response.QnaListResponse;
import koza.licensemanagementservice.domain.qna.entity.QnaStatus;
import koza.licensemanagementservice.domain.qna.service.QnaService;
import koza.licensemanagementservice.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/qna")
@Tag(name = "QNA API", description = "Q&A 문의 관리 API")
public class QnaController {
    private final QnaService qnaService;

    @Operation(summary = "전체 문의 목록", description = "검색, 상태 필터, 페이징 지원")
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllQuestions(@RequestParam(required = false) String search,
                                                          @RequestParam(required = false) QnaStatus status,
                                                          Pageable pageable) {
        Page<QnaListResponse> questions = qnaService.getAllQuestions(search, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(questions));
    }

    @Operation(summary = "소프트웨어별 문의 목록", description = "특정 소프트웨어의 문의 목록 조회")
    @GetMapping("/software/{softwareId}")
    public ResponseEntity<ApiResponse<?>> getQuestionsBySoftware(@PathVariable Long softwareId,
                                                                  @RequestParam(required = false) String search,
                                                                  @RequestParam(required = false) QnaStatus status,
                                                                  Pageable pageable) {
        Page<QnaListResponse> questions = qnaService.getQuestionsBySoftware(softwareId, search, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(questions));
    }

    @Operation(summary = "본인 문의 목록", description = "로그인한 사용자가 작성한 문의 목록 조회")
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<?>> getMyQuestions(@AuthenticationPrincipal CustomUser user,
                                                          @RequestParam(required = false) String search,
                                                          @RequestParam(required = false) QnaStatus status,
                                                          Pageable pageable) {
        Page<QnaListResponse> questions = qnaService.getMyQuestions(user, search, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(questions));
    }

    @Operation(summary = "본인 문의 목록 (소프트웨어별)", description = "로그인한 사용자가 특정 소프트웨어에 작성한 문의 목록 조회")
    @GetMapping("/my/software/{softwareId}")
    public ResponseEntity<ApiResponse<?>> getMyQuestionsBySoftware(@AuthenticationPrincipal CustomUser user,
                                                                    @PathVariable Long softwareId,
                                                                    @RequestParam(required = false) String search,
                                                                    @RequestParam(required = false) QnaStatus status,
                                                                    Pageable pageable) {
        Page<QnaListResponse> questions = qnaService.getMyQuestionsBySoftware(user, softwareId, search, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(questions));
    }

    @Operation(summary = "문의 단건 조회", description = "문의 상세 + 답변 조회")
    @GetMapping("/{qnaId}")
    public ResponseEntity<ApiResponse<?>> getQuestionDetail(@PathVariable Long qnaId) {
        QnaDetailResponse detail = qnaService.getQuestionDetail(qnaId);
        return ResponseEntity.ok(ApiResponse.success(detail));
    }

    @Operation(summary = "문의 등록")
    @PostMapping
    public ResponseEntity<ApiResponse<?>> createQuestion(@AuthenticationPrincipal CustomUser user,
                                                          @RequestBody @Valid QnaCreateRequest request) {
        QnaDetailResponse detail = qnaService.createQuestion(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(detail));
    }

    @Operation(summary = "문의 수정")
    @PatchMapping("/{qnaId}")
    public ResponseEntity<ApiResponse<?>> updateQuestion(@AuthenticationPrincipal CustomUser user,
                                                          @PathVariable Long qnaId,
                                                          @RequestBody @Valid QnaCreateRequest request) {
        QnaDetailResponse detail = qnaService.updateQuestion(user, qnaId, request);
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

    @Operation(summary = "상태 변경")
    @PatchMapping("/{qnaId}/status")
    public ResponseEntity<ApiResponse<?>> changeStatus(@AuthenticationPrincipal CustomUser user,
                                                        @PathVariable Long qnaId,
                                                        @RequestBody @Valid QnaStatusUpdateRequest request) {
        QnaDetailResponse detail = qnaService.changeStatus(user, qnaId, request);
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

    @Operation(summary = "문의 삭제")
    @DeleteMapping("/{qnaId}")
    public ResponseEntity<ApiResponse<?>> deleteQuestion(@AuthenticationPrincipal CustomUser user,
                                                          @PathVariable Long qnaId) {
        qnaService.deleteQuestion(user, qnaId);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
