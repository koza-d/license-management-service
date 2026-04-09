package koza.licensemanagementservice.domain.qna.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import koza.licensemanagementservice.auth.dto.CustomUser;
import koza.licensemanagementservice.auth.jwt.JwtTokenProvider;
import koza.licensemanagementservice.domain.qna.dto.request.QnaAnswerRequest;
import koza.licensemanagementservice.domain.qna.dto.request.QnaCreateRequest;
import koza.licensemanagementservice.domain.qna.dto.request.QnaStatusUpdateRequest;
import koza.licensemanagementservice.domain.qna.dto.response.QnaDetailResponse;
import koza.licensemanagementservice.domain.qna.dto.response.QnaListResponse;
import koza.licensemanagementservice.domain.qna.entity.QnaStatus;
import koza.licensemanagementservice.domain.qna.service.QnaService;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.CustomAccessDeniedHandler;
import koza.licensemanagementservice.global.error.CustomAuthenticationEntryPoint;
import koza.licensemanagementservice.global.config.SecurityConfig;
import koza.licensemanagementservice.global.error.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.HttpServletResponse;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(QnaController.class)
@Import(SecurityConfig.class)
class QnaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private QnaService qnaService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @MockitoBean
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    @BeforeEach
    void setUpEntryPoint() throws Exception {
        doAnswer(invocation -> {
            HttpServletResponse response = invocation.getArgument(1);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"error\":{\"code\":\"COMMON_004\",\"message\":\"로그인이 필요합니다.\"}}");
            return null;
        }).when(customAuthenticationEntryPoint).commence(any(), any(), any());
    }

    // === 헬퍼 ===

    private CustomUser customUser() {
        return new CustomUser("dev@test.com", 1L, "홍길동",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    private CustomUser adminUser() {
        return new CustomUser("admin@test.com", 99L, "관리자",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    private QnaDetailResponse detailResponse(Long id, QnaStatus status, String answer) {
        return QnaDetailResponse.builder()
                .qnaId(id)
                .softwareId(10L)
                .softwareName("MyApp")
                .nickname("홍길동")
                .title("라이센스 오류 문의")
                .content("라이센스 키 입력 시 오류가 발생합니다.")
                .status(status)
                .answer(answer)
                .answeredAt(answer != null ? LocalDateTime.of(2026, 4, 6, 12, 0) : null)
                .createAt(LocalDateTime.of(2026, 4, 1, 10, 0))
                .build();
    }

    private QnaListResponse listResponse(Long id) {
        return QnaListResponse.builder()
                .qnaId(id)
                .softwareName("MyApp")
                .nickname("홍길동")
                .title("라이센스 오류 문의")
                .status(QnaStatus.PENDING)
                .createAt(LocalDateTime.of(2026, 4, 1, 10, 0))
                .build();
    }

    // === 1. 전체 문의 목록 (GET /api/qna) ===

    @Nested
    @DisplayName("GET /api/qna - 전체 문의 목록")
    class GetAllQuestions {

        @Test
        @DisplayName("성공 - 인증 없이 목록 조회")
        void success() throws Exception {
            Page<QnaListResponse> page = new PageImpl<>(
                    List.of(listResponse(1L), listResponse(2L)),
                    PageRequest.of(0, 20), 2);

            given(qnaService.getAllQuestions(any(), any(), any())).willReturn(page);

            mockMvc.perform(get("/api/qna")
                            .param("page", "0")
                            .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content.length()").value(2))
                    .andExpect(jsonPath("$.data.content[0].qnaId").value(1))
                    .andExpect(jsonPath("$.data.content[0].softwareName").value("MyApp"));
        }

        @Test
        @DisplayName("성공 - 검색 + 상태 필터")
        void success_withSearchAndStatus() throws Exception {
            Page<QnaListResponse> page = new PageImpl<>(
                    List.of(listResponse(1L)), PageRequest.of(0, 20), 1);

            given(qnaService.getAllQuestions(eq("오류"), eq(QnaStatus.PENDING), any())).willReturn(page);

            mockMvc.perform(get("/api/qna")
                            .param("search", "오류")
                            .param("status", "PENDING"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content.length()").value(1));
        }

        @Test
        @DisplayName("성공 - 빈 결과")
        void success_empty() throws Exception {
            Page<QnaListResponse> page = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);

            given(qnaService.getAllQuestions(any(), any(), any())).willReturn(page);

            mockMvc.perform(get("/api/qna"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content.length()").value(0))
                    .andExpect(jsonPath("$.data.totalElements").value(0));
        }
    }

    // === 2. 소프트웨어별 문의 목록 (GET /api/qna/software/{softwareId}) ===

    @Nested
    @DisplayName("GET /api/qna/software/{softwareId} - 소프트웨어별 목록")
    class GetQuestionsBySoftware {

        @Test
        @DisplayName("성공")
        void success() throws Exception {
            Page<QnaListResponse> page = new PageImpl<>(
                    List.of(listResponse(1L)), PageRequest.of(0, 20), 1);

            given(qnaService.getQuestionsBySoftware(eq(10L), any(), any(), any())).willReturn(page);

            mockMvc.perform(get("/api/qna/software/10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content[0].qnaId").value(1));
        }
    }

    // === 3. 문의 단건 조회 (GET /api/qna/{qnaId}) ===

    @Nested
    @DisplayName("GET /api/qna/{qnaId} - 문의 상세 조회")
    class GetQuestionDetail {

        @Test
        @DisplayName("성공 - 답변 없는 문의")
        void success_withoutAnswer() throws Exception {
            given(qnaService.getQuestionDetail(1L))
                    .willReturn(detailResponse(1L, QnaStatus.PENDING, null));

            mockMvc.perform(get("/api/qna/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.qnaId").value(1))
                    .andExpect(jsonPath("$.data.softwareName").value("MyApp"))
                    .andExpect(jsonPath("$.data.status").value("PENDING"))
                    .andExpect(jsonPath("$.data.answer").doesNotExist());
        }

        @Test
        @DisplayName("성공 - 답변 있는 문의")
        void success_withAnswer() throws Exception {
            given(qnaService.getQuestionDetail(1L))
                    .willReturn(detailResponse(1L, QnaStatus.ANSWERED, "답변입니다."));

            mockMvc.perform(get("/api/qna/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("ANSWERED"))
                    .andExpect(jsonPath("$.data.answer").value("답변입니다."))
                    .andExpect(jsonPath("$.data.answeredAt").exists());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 문의")
        void fail_notFound() throws Exception {
            given(qnaService.getQuestionDetail(999L))
                    .willThrow(new BusinessException(ErrorCode.QNA_NOT_FOUND));

            mockMvc.perform(get("/api/qna/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("QNA_001"));
        }
    }

    // === 4. 문의 등록 (POST /api/qna) ===

    @Nested
    @DisplayName("POST /api/qna - 문의 등록")
    class CreateQuestion {

        @Test
        @DisplayName("성공")
        void success() throws Exception {
            QnaCreateRequest request = new QnaCreateRequest(10L, "라이센스 오류 문의", "오류가 발생합니다.");

            given(qnaService.createQuestion(any(CustomUser.class), any(QnaCreateRequest.class)))
                    .willReturn(detailResponse(5L, QnaStatus.PENDING, null));

            mockMvc.perform(post("/api/qna")
                            .with(user(customUser()))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.qnaId").value(5))
                    .andExpect(jsonPath("$.data.status").value("PENDING"));
        }

        @Test
        @DisplayName("실패 - 제목 누락 (Validation)")
        void fail_blankTitle() throws Exception {
            QnaCreateRequest request = new QnaCreateRequest(10L, "", "내용");

            mockMvc.perform(post("/api/qna")
                            .with(user(customUser()))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error.code").value("COMMON_001"))
                    .andExpect(jsonPath("$.error.errors.title").exists());
        }

        @Test
        @DisplayName("실패 - 소프트웨어 ID 누락 (Validation)")
        void fail_nullSoftwareId() throws Exception {
            QnaCreateRequest request = new QnaCreateRequest(null, "제목입니다", "내용");

            mockMvc.perform(post("/api/qna")
                            .with(user(customUser()))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.errors.softwareId").exists());
        }

        @Test
        @DisplayName("실패 - 내용 누락 (Validation)")
        void fail_blankContent() throws Exception {
            QnaCreateRequest request = new QnaCreateRequest(10L, "제목입니다", "");

            mockMvc.perform(post("/api/qna")
                            .with(user(customUser()))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.errors.content").exists());
        }

        @Test
        @DisplayName("실패 - 인증 없이 요청")
        void fail_unauthorized() throws Exception {
            QnaCreateRequest request = new QnaCreateRequest(10L, "제목", "내용입니다");

            mockMvc.perform(post("/api/qna")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // === 5. 문의 수정 (PATCH /api/qna/{qnaId}) ===

    @Nested
    @DisplayName("PATCH /api/qna/{qnaId} - 문의 수정")
    class UpdateQuestion {

        @Test
        @DisplayName("성공 - 본인 문의 수정")
        void success() throws Exception {
            QnaCreateRequest request = new QnaCreateRequest(10L, "수정된 제목", "수정된 내용");

            QnaDetailResponse updated = QnaDetailResponse.builder()
                    .qnaId(1L).softwareId(10L).softwareName("MyApp")
                    .nickname("홍길동").title("수정된 제목").content("수정된 내용")
                    .status(QnaStatus.PENDING)
                    .createAt(LocalDateTime.of(2026, 4, 1, 10, 0))
                    .build();

            given(qnaService.updateQuestion(any(CustomUser.class), eq(1L), any(QnaCreateRequest.class)))
                    .willReturn(updated);

            mockMvc.perform(patch("/api/qna/1")
                            .with(user(customUser()))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.title").value("수정된 제목"))
                    .andExpect(jsonPath("$.data.content").value("수정된 내용"));
        }

        @Test
        @DisplayName("실패 - 타인 문의 수정 (ACCESS_DENIED)")
        void fail_notOwner() throws Exception {
            QnaCreateRequest request = new QnaCreateRequest(10L, "제목", "내용입니다");

            given(qnaService.updateQuestion(any(CustomUser.class), eq(1L), any(QnaCreateRequest.class)))
                    .willThrow(new BusinessException(ErrorCode.ACCESS_DENIED));

            mockMvc.perform(patch("/api/qna/1")
                            .with(user(customUser()))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error.code").value("COMMON_003"));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 문의")
        void fail_notFound() throws Exception {
            QnaCreateRequest request = new QnaCreateRequest(10L, "제목", "내용입니다");

            given(qnaService.updateQuestion(any(CustomUser.class), eq(999L), any(QnaCreateRequest.class)))
                    .willThrow(new BusinessException(ErrorCode.QNA_NOT_FOUND));

            mockMvc.perform(patch("/api/qna/999")
                            .with(user(customUser()))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.code").value("QNA_001"));
        }
    }

    // === 6-1. 답변 시작 (POST /api/qna/{qnaId}/answer/start) ===

    @Nested
    @DisplayName("POST /api/qna/{qnaId}/answer/start - 답변 시작")
    class StartAnswering {

        @Test
        @DisplayName("성공 - PENDING → ANSWERING")
        void success() throws Exception {
            given(qnaService.startAnswering(any(CustomUser.class), eq(1L)))
                    .willReturn(detailResponse(1L, QnaStatus.ANSWERING, null));

            mockMvc.perform(post("/api/qna/1/answer/start")
                            .with(user(adminUser()))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("ANSWERING"));
        }

        @Test
        @DisplayName("실패 - 이미 답변 중")
        void fail_alreadyAnswering() throws Exception {
            given(qnaService.startAnswering(any(CustomUser.class), eq(1L)))
                    .willThrow(new BusinessException(ErrorCode.QNA_ALREADY_ANSWERING));

            mockMvc.perform(post("/api/qna/1/answer/start")
                            .with(user(adminUser()))
                            .with(csrf()))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error.code").value("QNA_004"));
        }

        @Test
        @DisplayName("실패 - 인증 없이 요청")
        void fail_unauthorized() throws Exception {
            mockMvc.perform(post("/api/qna/1/answer/start")
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());
        }
    }

    // === 6-2. 답변 취소 (POST /api/qna/{qnaId}/answer/cancel) ===

    @Nested
    @DisplayName("POST /api/qna/{qnaId}/answer/cancel - 답변 취소")
    class CancelAnswering {

        @Test
        @DisplayName("성공 - ANSWERING → PENDING")
        void success() throws Exception {
            given(qnaService.cancelAnswering(any(CustomUser.class), eq(1L)))
                    .willReturn(detailResponse(1L, QnaStatus.PENDING, null));

            mockMvc.perform(post("/api/qna/1/answer/cancel")
                            .with(user(adminUser()))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("PENDING"));
        }

        @Test
        @DisplayName("실패 - 답변 중 상태가 아님")
        void fail_notAnswering() throws Exception {
            given(qnaService.cancelAnswering(any(CustomUser.class), eq(1L)))
                    .willThrow(new BusinessException(ErrorCode.QNA_NOT_ANSWERING));

            mockMvc.perform(post("/api/qna/1/answer/cancel")
                            .with(user(adminUser()))
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("QNA_005"));
        }

        @Test
        @DisplayName("실패 - 인증 없이 요청")
        void fail_unauthorized() throws Exception {
            mockMvc.perform(post("/api/qna/1/answer/cancel")
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());
        }
    }

    // === 6-3. 답변 제출 (POST /api/qna/{qnaId}/answer) ===

    @Nested
    @DisplayName("POST /api/qna/{qnaId}/answer - 답변 제출")
    class SubmitAnswer {

        @Test
        @DisplayName("성공 - ANSWERING → ANSWERED")
        void success() throws Exception {
            QnaAnswerRequest request = new QnaAnswerRequest("답변 내용입니다.");

            given(qnaService.submitAnswer(any(CustomUser.class), eq(1L), any(QnaAnswerRequest.class)))
                    .willReturn(detailResponse(1L, QnaStatus.ANSWERED, "답변 내용입니다."));

            mockMvc.perform(post("/api/qna/1/answer")
                            .with(user(adminUser()))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.answer").value("답변 내용입니다."))
                    .andExpect(jsonPath("$.data.status").value("ANSWERED"));
        }

        @Test
        @DisplayName("실패 - 답변 내용 누락 (Validation)")
        void fail_blankAnswer() throws Exception {
            QnaAnswerRequest request = new QnaAnswerRequest("");

            mockMvc.perform(post("/api/qna/1/answer")
                            .with(user(adminUser()))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.errors.answer").exists());
        }

        @Test
        @DisplayName("실패 - PENDING 상태에서 바로 제출")
        void fail_notAnswering() throws Exception {
            QnaAnswerRequest request = new QnaAnswerRequest("답변");

            given(qnaService.submitAnswer(any(CustomUser.class), eq(1L), any(QnaAnswerRequest.class)))
                    .willThrow(new BusinessException(ErrorCode.QNA_NOT_ANSWERING));

            mockMvc.perform(post("/api/qna/1/answer")
                            .with(user(adminUser()))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code").value("QNA_005"));
        }

        @Test
        @DisplayName("실패 - 인증 없이 요청")
        void fail_unauthorized() throws Exception {
            QnaAnswerRequest request = new QnaAnswerRequest("답변");

            mockMvc.perform(post("/api/qna/1/answer")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // === 7. 상태 변경 (PATCH /api/qna/{qnaId}/status) ===

    @Nested
    @DisplayName("PATCH /api/qna/{qnaId}/status - 상태 변경")
    class ChangeStatus {

        @Test
        @DisplayName("성공 - 관리자가 CLOSED로 변경")
        void success_admin() throws Exception {
            QnaStatusUpdateRequest request = new QnaStatusUpdateRequest(QnaStatus.CLOSED);

            given(qnaService.changeStatus(any(CustomUser.class), eq(1L), any(QnaStatusUpdateRequest.class)))
                    .willReturn(detailResponse(1L, QnaStatus.CLOSED, null));

            mockMvc.perform(patch("/api/qna/1/status")
                            .with(user(adminUser()))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("CLOSED"));
        }

        @Test
        @DisplayName("실패 - 일반 사용자가 상태 변경 시도 (ACCESS_DENIED)")
        void fail_notAdmin() throws Exception {
            QnaStatusUpdateRequest request = new QnaStatusUpdateRequest(QnaStatus.CLOSED);

            given(qnaService.changeStatus(any(CustomUser.class), eq(1L), any(QnaStatusUpdateRequest.class)))
                    .willThrow(new BusinessException(ErrorCode.ACCESS_DENIED));

            mockMvc.perform(patch("/api/qna/1/status")
                            .with(user(customUser()))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error.code").value("COMMON_003"));
        }

        @Test
        @DisplayName("실패 - 상태값 누락 (Validation)")
        void fail_nullStatus() throws Exception {
            mockMvc.perform(patch("/api/qna/1/status")
                            .with(user(adminUser()))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status\": null}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.errors.status").exists());
        }

        @Test
        @DisplayName("실패 - 인증 없이 요청")
        void fail_unauthorized() throws Exception {
            mockMvc.perform(patch("/api/qna/1/status")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status\": \"CLOSED\"}"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // === 8. 문의 삭제 (DELETE /api/qna/{qnaId}) ===

    @Nested
    @DisplayName("DELETE /api/qna/{qnaId} - 문의 삭제")
    class DeleteQuestion {

        @Test
        @DisplayName("성공 - 본인 삭제")
        void success() throws Exception {
            willDoNothing().given(qnaService).deleteQuestion(any(CustomUser.class), eq(1L));

            mockMvc.perform(delete("/api/qna/1")
                            .with(user(customUser()))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @DisplayName("실패 - 타인 문의 삭제 (ACCESS_DENIED)")
        void fail_notOwner() throws Exception {
            willThrow(new BusinessException(ErrorCode.ACCESS_DENIED))
                    .given(qnaService).deleteQuestion(any(CustomUser.class), eq(1L));

            mockMvc.perform(delete("/api/qna/1")
                            .with(user(customUser()))
                            .with(csrf()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error.code").value("COMMON_003"));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 문의")
        void fail_notFound() throws Exception {
            willThrow(new BusinessException(ErrorCode.QNA_NOT_FOUND))
                    .given(qnaService).deleteQuestion(any(CustomUser.class), eq(999L));

            mockMvc.perform(delete("/api/qna/999")
                            .with(user(customUser()))
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.code").value("QNA_001"));
        }

        @Test
        @DisplayName("실패 - 인증 없이 요청")
        void fail_unauthorized() throws Exception {
            mockMvc.perform(delete("/api/qna/1")
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());
        }
    }
}
