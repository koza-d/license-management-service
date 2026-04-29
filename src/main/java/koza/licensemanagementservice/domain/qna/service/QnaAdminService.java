package koza.licensemanagementservice.domain.qna.service;

import koza.licensemanagementservice.auth.dto.user.CustomUser;
import koza.licensemanagementservice.domain.qna.dto.request.QnaAdminSearchCondition;
import koza.licensemanagementservice.domain.qna.dto.response.AdminQnaSummaryResponse;
import koza.licensemanagementservice.domain.qna.dto.response.QnaDetailResponse;
import koza.licensemanagementservice.domain.qna.repository.QnaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static koza.licensemanagementservice.global.validation.ValidUserAuthorized.validAdminAuthorized;

@Service
@RequiredArgsConstructor
public class QnaAdminService {
    private final QnaRepository qnaRepository;
    private final QnaService qnaService;

    @Transactional(readOnly = true)
    public Page<AdminQnaSummaryResponse> getQuestions(CustomUser admin, QnaAdminSearchCondition condition, Pageable pageable) {
        validAdminAuthorized(admin);
        condition.validateDateRanges();
        return qnaRepository.findByAdminCondition(condition, pageable);
    }

    @Transactional(readOnly = true)
    public QnaDetailResponse getQuestionDetail(CustomUser admin, Long qnaId) {
        validAdminAuthorized(admin);
        return qnaService.getQuestionDetail(qnaId);
    }
}
