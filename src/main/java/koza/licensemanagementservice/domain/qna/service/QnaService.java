package koza.licensemanagementservice.domain.qna.service;

import koza.licensemanagementservice.auth.dto.CustomUser;
import koza.licensemanagementservice.domain.qna.dto.request.QnaAnswerRequest;
import koza.licensemanagementservice.domain.qna.dto.request.QnaCreateRequest;
import koza.licensemanagementservice.domain.qna.dto.request.QnaPriorityUpdateRequest;
import koza.licensemanagementservice.domain.qna.dto.request.QnaStatusUpdateRequest;
import koza.licensemanagementservice.domain.qna.dto.response.QnaDetailResponse;
import koza.licensemanagementservice.domain.qna.dto.response.QnaListResponse;
import koza.licensemanagementservice.domain.member.entity.Member;
import koza.licensemanagementservice.domain.member.repository.MemberRepository;
import koza.licensemanagementservice.domain.qna.entity.Qna;
import koza.licensemanagementservice.domain.qna.entity.QnaPriority;
import koza.licensemanagementservice.domain.qna.entity.QnaStatus;
import koza.licensemanagementservice.domain.qna.log.dto.QnaAnswerUpdatedEvent;
import koza.licensemanagementservice.domain.qna.log.dto.QnaAnsweredEvent;
import koza.licensemanagementservice.domain.qna.log.dto.QnaPriorityChangedEvent;
import koza.licensemanagementservice.domain.qna.repository.QnaRepository;
import koza.licensemanagementservice.domain.software.entity.Software;
import koza.licensemanagementservice.domain.software.repository.SoftwareRepository;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QnaService {
    private final QnaRepository qnaRepository;
    private final SoftwareRepository softwareRepository;
    private final MemberRepository memberRepository;
    private final ApplicationEventPublisher eventPublisher;

    // 1. 전체 문의 목록
    @Transactional(readOnly = true)
    public Page<QnaListResponse> getAllQuestions(String search, QnaStatus status, Pageable pageable) {
        return qnaRepository.findAllQuestions(search, status, pageable);
    }

    // 2. 소프트웨어별 문의 목록
    @Transactional(readOnly = true)
    public Page<QnaListResponse> getQuestionsBySoftware(Long softwareId, String search, QnaStatus status, Pageable pageable) {
        return qnaRepository.findBySoftwareId(softwareId, search, status, pageable);
    }

    // 2-1. 본인 문의 목록
    @Transactional(readOnly = true)
    public Page<QnaListResponse> getMyQuestions(CustomUser user, String search, QnaStatus status, Pageable pageable) {
        return qnaRepository.findMyQuestions(user.getId(), null, search, status, pageable);
    }

    // 2-2. 본인 문의 목록 (소프트웨어별)
    @Transactional(readOnly = true)
    public Page<QnaListResponse> getMyQuestionsBySoftware(CustomUser user, Long softwareId, String search, QnaStatus status, Pageable pageable) {
        return qnaRepository.findMyQuestions(user.getId(), softwareId, search, status, pageable);
    }

    // 3. 문의 단건 조회
    @Transactional(readOnly = true)
    public QnaDetailResponse getQuestionDetail(Long qnaId) {
        Qna question = qnaRepository.findByIdWithSoftware(qnaId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QNA_NOT_FOUND));
        return QnaDetailResponse.from(question);
    }

    // 4. 문의 등록
    @Transactional
    public QnaDetailResponse createQuestion(CustomUser user, QnaCreateRequest request) {
        Software software = softwareRepository.findById(request.getSoftwareId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        Member member = memberRepository.getReferenceById(user.getId());

        Qna question = Qna.builder()
                .software(software)
                .member(member)
                .nickname(user.getNickname())
                .title(request.getTitle())
                .content(request.getContent())
                .priority(request.getPriorityOrDefault())
                .build();

        Qna saved = qnaRepository.save(question);
        return QnaDetailResponse.from(saved);
    }

    // 5. 문의 수정
    @Transactional
    public QnaDetailResponse updateQuestion(CustomUser user, Long qnaId, QnaCreateRequest request) {
        Qna question = findQuestion(qnaId);
        validateOwnerOrAdmin(user, question);

        Software software = softwareRepository.findById(request.getSoftwareId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        question.update(software, request.getTitle(), request.getContent());
        return QnaDetailResponse.from(question);
    }

    // 6. 답변 제출 (관리자 전용) — 기존 답변이 없으면 신규 등록, 있으면 수정으로 자동 분기
    @Transactional
    public QnaDetailResponse submitAnswer(CustomUser user, Long qnaId, QnaAnswerRequest request) {
        validateAdmin(user);
        Qna question = findQuestion(qnaId);
        Member asker = question.getMember();
        Long askerId = asker != null ? asker.getId() : null;
        String askerEmail = asker != null ? asker.getEmail() : null;

        String before = question.getAnswer();
        if (before == null) {
            question.submitAnswer(request.getAnswer());
            eventPublisher.publishEvent(new QnaAnsweredEvent(
                    user.getId(), question.getId(), question.getTitle(),
                    askerId, askerEmail));
        } else {
            question.updateAnswer(request.getAnswer());
            eventPublisher.publishEvent(new QnaAnswerUpdatedEvent(
                    user.getId(), question.getId(), question.getTitle(),
                    askerId, askerEmail,
                    before, request.getAnswer()));
        }
        return QnaDetailResponse.from(question);
    }

    // 7. 상태 변경 (관리자 전용)
    @Transactional
    public QnaDetailResponse changeStatus(CustomUser user, Long qnaId, QnaStatusUpdateRequest request) {
        validateAdmin(user);
        Qna question = findQuestion(qnaId);
        question.changeStatus(request.getStatus());
        return QnaDetailResponse.from(question);
    }

    // 7-1. 긴급도 변경 (관리자 전용)
    @Transactional
    public QnaDetailResponse changePriority(CustomUser user, Long qnaId, QnaPriorityUpdateRequest request) {
        validateAdmin(user);
        Qna question = findQuestion(qnaId);
        QnaPriority before = question.getPriority();
        question.changePriority(request.getPriority());
        if (before != request.getPriority()) {
            eventPublisher.publishEvent(new QnaPriorityChangedEvent(
                    user.getId(), question.getId(), question.getTitle(),
                    before, request.getPriority()));
        }
        return QnaDetailResponse.from(question);
    }

    // 8. 문의 삭제
    @Transactional
    public void deleteQuestion(CustomUser user, Long qnaId) {
        Qna question = findQuestion(qnaId);
        validateOwnerOrAdmin(user, question);
        qnaRepository.delete(question);
    }

    private Qna findQuestion(Long qnaId) {
        return qnaRepository.findById(qnaId)
                .orElseThrow(() -> new BusinessException(ErrorCode.QNA_NOT_FOUND));
    }

    private void validateOwnerOrAdmin(CustomUser user, Qna question) {
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
