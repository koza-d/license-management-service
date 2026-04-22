package koza.licensemanagementservice.domain.session.log.repository;

import koza.licensemanagementservice.domain.session.log.dto.SessionHistoryResponse;
import koza.licensemanagementservice.stat.dto.SoftwareUsageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;


public interface SessionLogRepositoryCustom {
    Page<SessionHistoryResponse> findByLicenseId(Long licenseId, Pageable pageable);
    Page<SessionHistoryResponse> findByLicenseId(Long licenseId, SessionLogSearchCondition condition, Pageable pageable);
    List<SoftwareUsageResponse> getTopNSoftwareByUsageTime(int n);
}
