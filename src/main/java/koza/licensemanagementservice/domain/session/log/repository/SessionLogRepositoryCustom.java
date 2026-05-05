package koza.licensemanagementservice.domain.session.log.repository;

import koza.licensemanagementservice.domain.session.log.dto.condition.SessionLogSearchCondition;
import koza.licensemanagementservice.domain.session.log.dto.response.DailyUsageResponse;
import koza.licensemanagementservice.domain.session.log.dto.response.SessionHistoryResponse;
import koza.licensemanagementservice.stat.dto.response.SoftwareUsageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


public interface SessionLogRepositoryCustom {
    Page<SessionHistoryResponse> findByLicenseId(Long licenseId, Pageable pageable);
    Page<SessionHistoryResponse> findByLicenseId(Long licenseId, SessionLogSearchCondition condition, Pageable pageable);
    List<SoftwareUsageResponse> getTopNSoftwareByUsageTime(int n);
    List<DailyUsageResponse> findDailyUsage(Long licenseId, LocalDateTime startDate);
}
