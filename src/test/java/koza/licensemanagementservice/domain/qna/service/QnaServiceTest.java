package koza.licensemanagementservice.domain.qna.service;

import koza.licensemanagementservice.auth.dto.CustomUser;
import koza.licensemanagementservice.domain.member.entity.Member;
import koza.licensemanagementservice.domain.member.repository.MemberRepository;
import koza.licensemanagementservice.domain.qna.dto.request.QnaAnswerRequest;
import koza.licensemanagementservice.domain.qna.dto.request.QnaCreateRequest;
import koza.licensemanagementservice.domain.qna.dto.request.QnaStatusUpdateRequest;
import koza.licensemanagementservice.domain.qna.dto.response.QnaDetailResponse;
import koza.licensemanagementservice.domain.qna.entity.QnaQuestion;
import koza.licensemanagementservice.domain.qna.entity.QnaStatus;
import koza.licensemanagementservice.domain.qna.repository.QnaQuestionRepository;
import koza.licensemanagementservice.domain.software.entity.Software;
import koza.licensemanagementservice.domain.software.repository.SoftwareRepository;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class QnaServiceTest {

    @InjectMocks
    private QnaService qnaService;

    @Mock
    private QnaQuestionRepository qnaQuestionRepository;
    @Mock
    private SoftwareRepository softwareRepository;
    @Mock
    private MemberRepository memberRepository;

    // === 테스트 헬퍼 ===

    private Member createMember(Long id) {
        return Member.builder().id(id).email("dev@test.com").nickname("홍길동").build();
    }

    private Software createSoftware(Long id, Member member) {
        return Software.builder().id(id).member(member).name("MyApp")
                .latestVersion("1.0.0").apiKey("key").limitLicense(100).build();
    }

    private CustomUser createCustomUser(Long id) {
        return new CustomUser("dev@test.com", id, "홍길동",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    private CustomUser createAdminUser(Long id) {
        return new CustomUser("admin@test.com", id, "관리자",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    private QnaQuestion createQuestion(Long id, Software software, Member member) {
        return QnaQuestion.builder()
                .id(id).software(software).member(member)
                .nickname("홍길동").title("라이센스 오류 문의")
                .content("라이센스 키 입력 시 오류가 발생합니다.")
                .build();
    }

    // === 3. 문의 단건 조회 ===

    @Nested
    @DisplayName("문의 단건 조회")
    class GetQuestionDetail {

        @Test
        @DisplayName("성공 - 답변 없는 문의")
        void success_withoutAnswer() {
            Member member = createMember(1L);
            Software software = createSoftware(10L, member);
            QnaQuestion question = createQuestion(1L, software, member);

            given(qnaQuestionRepository.findByIdWithSoftware(1L)).willReturn(Optional.of(question));

            QnaDetailResponse response = qnaService.getQuestionDetail(1L);

            assertThat(response.getQnaId()).isEqualTo(1L);
            assertThat(response.getSoftwareId()).isEqualTo(10L);
            assertThat(response.getSoftwareName()).isEqualTo("MyApp");
            assertThat(response.getNickname()).isEqualTo("홍길동");
            assertThat(response.getStatus()).isEqualTo(QnaStatus.PENDING);
            assertThat(response.getAnswer()).isNull();
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 문의")
        void fail_notFound() {
            given(qnaQuestionRepository.findByIdWithSoftware(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> qnaService.getQuestionDetail(999L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getError())
                    .isEqualTo(ErrorCode.QNA_NOT_FOUND);
        }
    }

    // === 4. 문의 등록 ===

    @Nested
    @DisplayName("문의 등록")
    class CreateQuestion {

        @Test
        @DisplayName("성공 - member가 설정됨")
        void success() {
            Member member = createMember(1L);
            Software software = createSoftware(10L, member);
            CustomUser user = createCustomUser(1L);
            QnaCreateRequest request = new QnaCreateRequest(10L, "라이센스 오류 문의", "오류가 발생합니다.");

            given(softwareRepository.findById(10L)).willReturn(Optional.of(software));
            given(memberRepository.getReferenceById(1L)).willReturn(member);

            QnaQuestion saved = QnaQuestion.builder()
                    .id(5L).software(software).member(member)
                    .nickname("홍길동").title("라이센스 오류 문의")
                    .content("오류가 발생합니다.")
                    .build();
            given(qnaQuestionRepository.save(any(QnaQuestion.class))).willReturn(saved);

            QnaDetailResponse response = qnaService.createQuestion(user, request);

            assertThat(response.getQnaId()).isEqualTo(5L);
            assertThat(response.getSoftwareName()).isEqualTo("MyApp");
            assertThat(response.getStatus()).isEqualTo(QnaStatus.PENDING);
            verify(qnaQuestionRepository).save(any(QnaQuestion.class));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 소프트웨어")
        void fail_softwareNotFound() {
            CustomUser user = createCustomUser(1L);
            QnaCreateRequest request = new QnaCreateRequest(999L, "제목", "내용");

            given(softwareRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> qnaService.createQuestion(user, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getError())
                    .isEqualTo(ErrorCode.NOT_FOUND);
        }
    }

    // === 5. 문의 수정 ===

    @Nested
    @DisplayName("문의 수정")
    class UpdateQuestion {

        @Test
        @DisplayName("성공 - 본인 문의 수정")
        void success_owner() {
            Member member = createMember(1L);
            Software software = createSoftware(10L, member);
            QnaQuestion question = createQuestion(1L, software, member);
            CustomUser user = createCustomUser(1L);
            QnaCreateRequest request = new QnaCreateRequest(10L, "수정된 제목", "수정된 내용");

            given(qnaQuestionRepository.findById(1L)).willReturn(Optional.of(question));
            given(softwareRepository.findById(10L)).willReturn(Optional.of(software));

            QnaDetailResponse response = qnaService.updateQuestion(user, 1L, request);

            assertThat(response.getTitle()).isEqualTo("수정된 제목");
            assertThat(response.getContent()).isEqualTo("수정된 내용");
        }

        @Test
        @DisplayName("성공 - 관리자가 타인 문의 수정")
        void success_admin() {
            Member member = createMember(1L);
            Software software = createSoftware(10L, member);
            QnaQuestion question = createQuestion(1L, software, member);
            CustomUser admin = createAdminUser(99L);
            QnaCreateRequest request = new QnaCreateRequest(10L, "관리자 수정", "관리자 내용");

            given(qnaQuestionRepository.findById(1L)).willReturn(Optional.of(question));
            given(softwareRepository.findById(10L)).willReturn(Optional.of(software));

            QnaDetailResponse response = qnaService.updateQuestion(admin, 1L, request);

            assertThat(response.getTitle()).isEqualTo("관리자 수정");
        }

        @Test
        @DisplayName("실패 - 타인 문의 수정 시 ACCESS_DENIED")
        void fail_notOwner() {
            Member member = createMember(1L);
            Software software = createSoftware(10L, member);
            QnaQuestion question = createQuestion(1L, software, member);
            CustomUser otherUser = createCustomUser(2L);
            QnaCreateRequest request = new QnaCreateRequest(10L, "제목", "내용");

            given(qnaQuestionRepository.findById(1L)).willReturn(Optional.of(question));

            assertThatThrownBy(() -> qnaService.updateQuestion(otherUser, 1L, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getError())
                    .isEqualTo(ErrorCode.ACCESS_DENIED);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 문의")
        void fail_questionNotFound() {
            CustomUser user = createCustomUser(1L);
            QnaCreateRequest request = new QnaCreateRequest(10L, "제목", "내용");

            given(qnaQuestionRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> qnaService.updateQuestion(user, 999L, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getError())
                    .isEqualTo(ErrorCode.QNA_NOT_FOUND);
        }
    }

    // === 6-1. 답변 시작 ===

    @Nested
    @DisplayName("답변 시작")
    class StartAnswering {

        @Test
        @DisplayName("성공 - PENDING → ANSWERING")
        void success() {
            Member member = createMember(1L);
            Software software = createSoftware(10L, member);
            QnaQuestion question = createQuestion(1L, software, member);
            CustomUser admin = createAdminUser(99L);

            given(qnaQuestionRepository.findById(1L)).willReturn(Optional.of(question));

            QnaDetailResponse response = qnaService.startAnswering(admin, 1L);

            assertThat(response.getStatus()).isEqualTo(QnaStatus.ANSWERING);
        }

        @Test
        @DisplayName("실패 - 이미 답변 중")
        void fail_alreadyAnswering() {
            Member member = createMember(1L);
            Software software = createSoftware(10L, member);
            QnaQuestion question = createQuestion(1L, software, member);
            question.startAnswering();
            CustomUser admin = createAdminUser(99L);

            given(qnaQuestionRepository.findById(1L)).willReturn(Optional.of(question));

            assertThatThrownBy(() -> qnaService.startAnswering(admin, 1L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getError())
                    .isEqualTo(ErrorCode.QNA_ALREADY_ANSWERING);
        }

        @Test
        @DisplayName("실패 - 이미 답변 완료")
        void fail_alreadyAnswered() {
            Member member = createMember(1L);
            Software software = createSoftware(10L, member);
            QnaQuestion question = createQuestion(1L, software, member);
            question.startAnswering();
            question.submitAnswer("답변");
            CustomUser admin = createAdminUser(99L);

            given(qnaQuestionRepository.findById(1L)).willReturn(Optional.of(question));

            assertThatThrownBy(() -> qnaService.startAnswering(admin, 1L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getError())
                    .isEqualTo(ErrorCode.QNA_ALREADY_ANSWERED);
        }

        @Test
        @DisplayName("실패 - 일반 사용자")
        void fail_notAdmin() {
            CustomUser user = createCustomUser(1L);

            assertThatThrownBy(() -> qnaService.startAnswering(user, 1L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getError())
                    .isEqualTo(ErrorCode.ACCESS_DENIED);
        }
    }

    // === 6-2. 답변 취소 ===

    @Nested
    @DisplayName("답변 취소")
    class CancelAnswering {

        @Test
        @DisplayName("성공 - ANSWERING → PENDING")
        void success() {
            Member member = createMember(1L);
            Software software = createSoftware(10L, member);
            QnaQuestion question = createQuestion(1L, software, member);
            question.startAnswering();
            CustomUser admin = createAdminUser(99L);

            given(qnaQuestionRepository.findById(1L)).willReturn(Optional.of(question));

            QnaDetailResponse response = qnaService.cancelAnswering(admin, 1L);

            assertThat(response.getStatus()).isEqualTo(QnaStatus.PENDING);
        }

        @Test
        @DisplayName("실패 - PENDING 상태에서 취소 시도")
        void fail_notAnswering() {
            Member member = createMember(1L);
            Software software = createSoftware(10L, member);
            QnaQuestion question = createQuestion(1L, software, member);
            CustomUser admin = createAdminUser(99L);

            given(qnaQuestionRepository.findById(1L)).willReturn(Optional.of(question));

            assertThatThrownBy(() -> qnaService.cancelAnswering(admin, 1L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getError())
                    .isEqualTo(ErrorCode.QNA_NOT_ANSWERING);
        }

        @Test
        @DisplayName("실패 - 일반 사용자")
        void fail_notAdmin() {
            CustomUser user = createCustomUser(1L);

            assertThatThrownBy(() -> qnaService.cancelAnswering(user, 1L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getError())
                    .isEqualTo(ErrorCode.ACCESS_DENIED);
        }
    }

    // === 6-3. 답변 제출 ===

    @Nested
    @DisplayName("답변 제출")
    class SubmitAnswer {

        @Test
        @DisplayName("성공 - ANSWERING → ANSWERED")
        void success() {
            Member member = createMember(1L);
            Software software = createSoftware(10L, member);
            QnaQuestion question = createQuestion(1L, software, member);
            question.startAnswering();
            CustomUser admin = createAdminUser(99L);
            QnaAnswerRequest request = new QnaAnswerRequest("답변 내용입니다.");

            given(qnaQuestionRepository.findById(1L)).willReturn(Optional.of(question));

            QnaDetailResponse response = qnaService.submitAnswer(admin, 1L, request);

            assertThat(response.getAnswer()).isEqualTo("답변 내용입니다.");
            assertThat(response.getAnsweredAt()).isNotNull();
            assertThat(response.getStatus()).isEqualTo(QnaStatus.ANSWERED);
        }

        @Test
        @DisplayName("실패 - PENDING 상태에서 바로 제출 시도")
        void fail_notAnswering() {
            Member member = createMember(1L);
            Software software = createSoftware(10L, member);
            QnaQuestion question = createQuestion(1L, software, member);
            CustomUser admin = createAdminUser(99L);
            QnaAnswerRequest request = new QnaAnswerRequest("답변");

            given(qnaQuestionRepository.findById(1L)).willReturn(Optional.of(question));

            assertThatThrownBy(() -> qnaService.submitAnswer(admin, 1L, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getError())
                    .isEqualTo(ErrorCode.QNA_NOT_ANSWERING);
        }

        @Test
        @DisplayName("실패 - 일반 사용자")
        void fail_notAdmin() {
            CustomUser user = createCustomUser(1L);
            QnaAnswerRequest request = new QnaAnswerRequest("답변");

            assertThatThrownBy(() -> qnaService.submitAnswer(user, 1L, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getError())
                    .isEqualTo(ErrorCode.ACCESS_DENIED);
        }
    }

    // === 7. 상태 변경 ===

    @Nested
    @DisplayName("상태 변경")
    class ChangeStatus {

        @Test
        @DisplayName("성공 - 관리자가 CLOSED로 변경")
        void success_admin() {
            Member member = createMember(1L);
            Software software = createSoftware(10L, member);
            QnaQuestion question = createQuestion(1L, software, member);
            CustomUser admin = createAdminUser(99L);
            QnaStatusUpdateRequest request = new QnaStatusUpdateRequest(QnaStatus.CLOSED);

            given(qnaQuestionRepository.findById(1L)).willReturn(Optional.of(question));

            QnaDetailResponse response = qnaService.changeStatus(admin, 1L, request);

            assertThat(response.getStatus()).isEqualTo(QnaStatus.CLOSED);
        }

        @Test
        @DisplayName("실패 - 일반 사용자가 상태 변경 시도")
        void fail_notAdmin() {
            CustomUser user = createCustomUser(1L);
            QnaStatusUpdateRequest request = new QnaStatusUpdateRequest(QnaStatus.CLOSED);

            assertThatThrownBy(() -> qnaService.changeStatus(user, 1L, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getError())
                    .isEqualTo(ErrorCode.ACCESS_DENIED);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 문의")
        void fail_notFound() {
            CustomUser admin = createAdminUser(99L);
            QnaStatusUpdateRequest request = new QnaStatusUpdateRequest(QnaStatus.CLOSED);

            given(qnaQuestionRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> qnaService.changeStatus(admin, 999L, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getError())
                    .isEqualTo(ErrorCode.QNA_NOT_FOUND);
        }
    }

    // === 8. 문의 삭제 ===

    @Nested
    @DisplayName("문의 삭제")
    class DeleteQuestion {

        @Test
        @DisplayName("성공 - 본인 삭제")
        void success_owner() {
            Member member = createMember(1L);
            Software software = createSoftware(10L, member);
            QnaQuestion question = createQuestion(1L, software, member);
            CustomUser user = createCustomUser(1L);

            given(qnaQuestionRepository.findById(1L)).willReturn(Optional.of(question));

            qnaService.deleteQuestion(user, 1L);

            verify(qnaQuestionRepository).delete(question);
        }

        @Test
        @DisplayName("성공 - 관리자가 타인 문의 삭제")
        void success_admin() {
            Member member = createMember(1L);
            Software software = createSoftware(10L, member);
            QnaQuestion question = createQuestion(1L, software, member);
            CustomUser admin = createAdminUser(99L);

            given(qnaQuestionRepository.findById(1L)).willReturn(Optional.of(question));

            qnaService.deleteQuestion(admin, 1L);

            verify(qnaQuestionRepository).delete(question);
        }

        @Test
        @DisplayName("실패 - 타인 문의 삭제 시 ACCESS_DENIED")
        void fail_notOwner() {
            Member member = createMember(1L);
            Software software = createSoftware(10L, member);
            QnaQuestion question = createQuestion(1L, software, member);
            CustomUser otherUser = createCustomUser(2L);

            given(qnaQuestionRepository.findById(1L)).willReturn(Optional.of(question));

            assertThatThrownBy(() -> qnaService.deleteQuestion(otherUser, 1L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getError())
                    .isEqualTo(ErrorCode.ACCESS_DENIED);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 문의")
        void fail_notFound() {
            CustomUser user = createCustomUser(1L);

            given(qnaQuestionRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> qnaService.deleteQuestion(user, 999L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getError())
                    .isEqualTo(ErrorCode.QNA_NOT_FOUND);
        }
    }
}
