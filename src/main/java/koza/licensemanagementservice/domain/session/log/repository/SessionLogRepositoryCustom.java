package koza.licensemanagementservice.domain.session.log.repository;

import koza.licensemanagementservice.domain.session.log.dto.SessionHistoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface SessionLogRepositoryCustom {
    Page<SessionHistoryResponse> findByLicenseId(Long licenseId, Pageable pageable);
    Page<SessionHistoryResponse> findByLicenseId(Long licenseId, SessionLogSearchCondition condition, Pageable pageable);
}
