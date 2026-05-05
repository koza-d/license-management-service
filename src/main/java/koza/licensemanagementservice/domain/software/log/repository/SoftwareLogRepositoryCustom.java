package koza.licensemanagementservice.domain.software.log.repository;

import koza.licensemanagementservice.domain.software.log.dto.condition.SoftwareLogSearchCondition;
import koza.licensemanagementservice.domain.software.log.dto.response.SoftwareLogResponse;
import koza.licensemanagementservice.stat.dto.response.SoftwareRegisterTrendResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface SoftwareLogRepositoryCustom {
    Page<SoftwareLogResponse> findBySoftwareId(Long softwareId, SoftwareLogSearchCondition condition, Pageable pageable);

    List<SoftwareRegisterTrendResponse> getSoftwareRegistrationTrends(LocalDate from, LocalDate to);
}
