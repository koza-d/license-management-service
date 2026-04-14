package koza.licensemanagementservice.domain.session.log.repository;

import koza.licensemanagementservice.domain.session.log.entity.SessionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface SessionLogRepositoryCustom {
    Page<SessionLog> findByLicenseId(Long licenseId, Pageable pageable);
    Page<SessionLog> findByLicenseId(Long licenseId, LocalDate startDate, LocalDate endDate, Pageable pageable);
}
