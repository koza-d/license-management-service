package koza.licensemanagementservice.domain.qna.service;

import koza.licensemanagementservice.auth.dto.CustomUser;
import koza.licensemanagementservice.domain.qna.dto.request.QnaAnswerRequest;
import koza.licensemanagementservice.domain.qna.dto.request.QnaCreateRequest;
import koza.licensemanagementservice.domain.qna.dto.request.QnaStatusUpdateRequest;
import koza.licensemanagementservice.domain.qna.dto.response.QnaDetailResponse;
import koza.licensemanagementservice.domain.qna.dto.response.QnaListResponse;
import koza.licensemanagementservice.domain.member.entity.Member;
import koza.licensemanagementservice.domain.member.repository.MemberRepository;
import koza.licensemanagementservice.domain.qna.entity.QnaQuestion;
import koza.licensemanagementservice.domain.qna.entity.QnaStatus;
import koza.licensemanagementservice.domain.qna.repository.QnaQuestionRepository;
import koza.licensemanagementservice.domain.software.entity.Software;
import koza.licensemanagementservice.domain.software.repository.SoftwareRepository;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QnaService {
    private final QnaQuestionRepository qnaQuestionRepository;
    private final SoftwareRepository softwareRepository;
    private final MemberRepository memberRepository;

    // 1. 전체 문의 목록
    @Transactional(readOnly = true)
    public Page<QnaListResponse> getAllQuestions(String search, QnaStatus status, Pageable pageable) {
        return qnaQuestionRepository.findAllQuestions(search, status, pageable);
    }

    // 2. 소프트웨어별 문의 목록
    @Transactional(readOnly = true)
    public Page<QnaListResponse> getQuestionsBySoftware(Long softwareId, String search, QnaStatus status, Pageable pageable) {
        return qnaQuestionRepository.findBySoftwareId(softwareId, search, status, pageable);
    }

    // 3. 문의 단건 조회
    @Transactional(readOnly = true)
    public QnaDetailResponse getQuestionDetail(Long qnaId) {
        QnaQuestion question = qnaQuestionRepository.findByIdWithSoftware(qnaId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QNA_NOT_FOUND));
        return QnaDetailResponse.from(question);
    }

    // 4. 문의 등록
    @Transactional
    public QnaDetailResponse createQuestion(CustomUser user, QnaCreateRequest request) {
        Software software = softwareRepository.findById(request.getSoftwareId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        Member member = memberRepository.getReferenceById(user.getId());

        QnaQuestion question = QnaQuestion.builder()
                .software(software)
                .member(member)
                .nickname(user.getNickname())
                .title(request.getTitle())
                .content(request.getContent())
                .build();

        QnaQuestion saved = qnaQuestionRepository.save(question);
        return QnaDetailResponse.from(saved);
    }

    // 5. 문의 수정
    @Transactional
    public QnaDetailResponse updateQuestion(CustomUser user, Long qnaId, QnaCreateRequest request) {
        QnaQuestion question = findQuestion(qnaId);
        validateOwnerOrAdmin(user, question);

        Software software = softwareRepository.findById(request.getSoftwareId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        question.update(software, request.getTitle(), request.getContent());
        return QnaDetailResponse.from(question);
    }

    // 6. 답변 제출 (관리자 전용)
    @Transactional
    public QnaDetailResponse submitAnswer(CustomUser user, Long qnaId, QnaAnswerRequest request) {
        validateAdmin(user);
        QnaQuestion question = findQuestion(qnaId);
        question.submitAnswer(request.getAnswer());
        return QnaDetailResponse.from(question);
    }

    // 7. 상태 변경 (관리자 전용)
    @Transactional
    public QnaDetailResponse changeStatus(CustomUser user, Long qnaId, QnaStatusUpdateRequest request) {
        validateAdmin(user);
        QnaQuestion question = findQuestion(qnaId);
        question.changeStatus(request.getStatus());
        return QnaDetailResponse.from(question);
    }

    // 8. 문의 삭제
    @Transactional
    public void deleteQuestion(CustomUser user, Long qnaId) {
        QnaQuestion question = findQuestion(qnaId);
        validateOwnerOrAdmin(user, question);
        qnaQuestionRepository.delete(question);
    }

    private QnaQuestion findQuestion(Long qnaId) {
        return qnaQuestionRepository.findById(qnaId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QNA_NOT_FOUND));
    }

    private void validateOwnerOrAdmin(CustomUser user, QnaQuestion question) {
        if (isAdmin(user)) return;
        if (question.getMember() == null || !question.getMember().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
    }

    private void validateAdmin(CustomUser user) {
        if (!isAdmin(user)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
    }

    private boolean isAdmin(CustomUser user) {
        return user.getAuthorities().stream()
                .anyMatch(auth -> "ROLE_ADMIN".equals(auth.getAuthority()));
    }
}
