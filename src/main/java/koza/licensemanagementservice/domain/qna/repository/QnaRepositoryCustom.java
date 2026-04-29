package koza.licensemanagementservice.domain.qna.repository;

import koza.licensemanagementservice.dashboard.dto.response.PendingQnaResponse;
import koza.licensemanagementservice.domain.qna.dto.request.QnaAdminSearchCondition;
import koza.licensemanagementservice.domain.qna.dto.response.AdminQnaSummaryResponse;
import koza.licensemanagementservice.domain.qna.dto.response.QnaListResponse;
import koza.licensemanagementservice.domain.qna.entity.QnaStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface QnaRepositoryCustom {
    Page<QnaListResponse> findAllQuestions(String search, QnaStatus status, Pageable pageable);
    Page<QnaListResponse> findBySoftwareId(Long softwareId, String search, QnaStatus status, Pageable pageable);
    Page<QnaListResponse> findMyQuestions(Long memberId, Long softwareId, String search, QnaStatus status, Pageable pageable);

    Page<AdminQnaSummaryResponse> findByAdminCondition(QnaAdminSearchCondition condition, Pageable pageable);

    List<PendingQnaResponse> findPendingForDashboard(int limit);
}
