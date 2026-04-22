package koza.licensemanagementservice.domain.qna.service;

import koza.licensemanagementservice.domain.qna.dto.request.QnaAdminSearchCondition;
import koza.licensemanagementservice.domain.qna.dto.response.QnaAdminListResponse;
import koza.licensemanagementservice.domain.qna.repository.QnaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class QnaAdminService {
    private final QnaRepository qnaRepository;

    @Transactional(readOnly = true)
    public Page<QnaAdminListResponse> getQuestions(QnaAdminSearchCondition condition, Pageable pageable) {
        condition.validateDateRanges();
        return qnaRepository.findByAdminCondition(condition, pageable);
    }
}
