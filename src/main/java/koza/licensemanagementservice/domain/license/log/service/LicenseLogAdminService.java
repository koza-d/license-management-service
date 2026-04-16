package koza.licensemanagementservice.domain.license.log.service;

import koza.licensemanagementservice.auth.dto.CustomUser;
import koza.licensemanagementservice.domain.license.log.dto.LicenseExtendLogResponse;
import koza.licensemanagementservice.domain.license.log.dto.LicenseLogResponse;
import koza.licensemanagementservice.domain.license.log.repository.LicenseExtendLogRepository;
import koza.licensemanagementservice.domain.license.log.repository.LicenseLogRepository;
import koza.licensemanagementservice.domain.license.log.repository.LicenseLogSearchCondition;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

import static koza.licensemanagementservice.global.validation.ValidUserAuthorized.validAdminAuthorized;

@Service
@RequiredArgsConstructor
public class LicenseLogAdminService {
    private final LicenseExtendLogRepository extendLogRepository;
    private final LicenseLogRepository logRepository;

    public Page<LicenseExtendLogResponse> getLicenseExtendLogs(CustomUser user, Long licenseId, LocalDate from, LocalDate to, Pageable pageable) {
        validAdminAuthorized(user);

        return extendLogRepository.findByLicenseId(licenseId, from, to, pageable);
    }

    public Page<LicenseLogResponse> getLicenseChangedLogs(CustomUser user, Long licenseId, LicenseLogSearchCondition condition, Pageable pageable) {
        validAdminAuthorized(user);

        return logRepository.findByLicenseId(licenseId, condition, pageable);
    }
}
