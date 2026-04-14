package koza.licensemanagementservice.domain.license.log.service;

import koza.licensemanagementservice.auth.dto.CustomUser;
import koza.licensemanagementservice.domain.license.log.dto.LicenseExtendLogResponse;
import koza.licensemanagementservice.domain.license.log.repository.LicenseExtendLogRepository;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LicenseLogAdminService {
    private final LicenseExtendLogRepository extendLogRepository;

    public Page<LicenseExtendLogResponse> getLicenseExtendLogs(CustomUser user, Long licenseId, LocalDate from, LocalDate to, Pageable pageable) {
        validAdminAuthorized(user);

        return extendLogRepository.findByLicenseId(licenseId, from, to, pageable);
    }

    private static void validAdminAuthorized(CustomUser user) {
        user.getAuthorities().stream()
                .filter(auth -> auth.toString().equals("ADMIN"))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCESS_DENIED));
    }
}
