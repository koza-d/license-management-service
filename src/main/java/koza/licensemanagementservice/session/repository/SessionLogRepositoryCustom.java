package koza.licensemanagementservice.session.repository;

import koza.licensemanagementservice.session.entity.SessionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface SessionLogRepositoryCustom {
    Page<SessionLog> findByLicenseId(Long licenseId, Pageable pageable);
    Page<SessionLog> findByLicenseId(Long licenseId, LocalDate startDate, LocalDate endDate, Pageable pageable);
}
