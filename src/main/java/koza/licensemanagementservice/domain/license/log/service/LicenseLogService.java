package koza.licensemanagementservice.domain.license.log.service;

import koza.licensemanagementservice.auth.dto.user.CustomUser;
import koza.licensemanagementservice.domain.license.entity.License;
import koza.licensemanagementservice.domain.license.log.dto.response.LicenseExtendLogResponse;
import koza.licensemanagementservice.domain.license.log.repository.LicenseExtendLogRepository;
import koza.licensemanagementservice.domain.license.repository.LicenseRepository;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class LicenseLogService {
    private final LicenseExtendLogRepository extendLogRepository;
    private final LicenseRepository licenseRepository;

    @Transactional(readOnly = true)
    public Page<LicenseExtendLogResponse> getLicenseExtendLogs(CustomUser user, Long licenseId, LocalDate from, LocalDate to, Pageable pageable) {
        validateLicenseOwner(user, licenseId);

        if (from != null && to != null && from.isAfter(to))
            throw new BusinessException(ErrorCode.INVALID_REQUEST);

        return extendLogRepository.findByLicenseId(licenseId, from, to, pageable);
    }

    private void validateLicenseOwner(CustomUser user, Long licenseId) {
        License license = licenseRepository.findByIdWithSoftwareAndMember(licenseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
        if (!license.getSoftware().getMember().getId().equals(user.getId()))
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
    }
}
