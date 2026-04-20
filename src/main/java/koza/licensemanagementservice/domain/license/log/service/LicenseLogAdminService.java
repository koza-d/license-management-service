package koza.licensemanagementservice.domain.license.log.service;

import koza.licensemanagementservice.auth.dto.CustomUser;
import koza.licensemanagementservice.domain.license.entity.License;
import koza.licensemanagementservice.domain.license.log.dto.LicenseExtendLogResponse;
import koza.licensemanagementservice.domain.license.log.dto.LicenseLogResponse;
import koza.licensemanagementservice.domain.license.log.entity.LicenseLog;
import koza.licensemanagementservice.domain.license.log.entity.LicenseLogType;
import koza.licensemanagementservice.domain.license.log.repository.LicenseExtendLogRepository;
import koza.licensemanagementservice.domain.license.log.repository.LicenseLogRepository;
import koza.licensemanagementservice.domain.license.log.repository.LicenseLogSearchCondition;
import koza.licensemanagementservice.domain.license.repository.LicenseRepository;
import koza.licensemanagementservice.domain.member.entity.Member;
import koza.licensemanagementservice.domain.member.repository.MemberRepository;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static koza.licensemanagementservice.global.validation.ValidUserAuthorized.validAdminAuthorized;

@Service
@RequiredArgsConstructor
public class LicenseLogAdminService {
    private final MemberRepository memberRepository;
    private final LicenseExtendLogRepository extendLogRepository;
    private final LicenseLogRepository logRepository;
    private final LicenseRepository licenseRepository;

    @Transactional(readOnly = true)
    public Page<LicenseExtendLogResponse> getLicenseExtendLogs(CustomUser user, Long licenseId, LocalDate from, LocalDate to, Pageable pageable) {
        validAdminAuthorized(user);

        if (from != null && to != null && from.isAfter(to))
            throw new BusinessException(ErrorCode.INVALID_REQUEST);

        return extendLogRepository.findByLicenseId(licenseId, from, to, pageable);
    }

    @Transactional(readOnly = true)
    public Page<LicenseLogResponse> getLicenseChangedLogs(CustomUser user, Long licenseId, LicenseLogSearchCondition condition, Pageable pageable) {
        validAdminAuthorized(user);

        if (condition.getFrom() != null && condition.getTo() != null
                && condition.getFrom().isAfter(condition.getTo()))
            throw new BusinessException(ErrorCode.INVALID_REQUEST);

        return logRepository.findByLicenseId(licenseId, condition, pageable);
    }

    @Transactional
    public void recordExpiredLicenseLogs(boolean isSystem, CustomUser user) {
        if (!isSystem)
            validAdminAuthorized(user);

        LocalDateTime now = LocalDateTime.now();
        List<LicenseLog> licenseLogs = new ArrayList<>();
        List<License> target = licenseRepository.findByExpiredWithoutLog(now);
        target.forEach(license -> {
            Member operator = memberRepository.getReferenceById(isSystem ? -1L : user.getId());
            LicenseLog licenseLog = LicenseLog.builder()
                    .operator(operator)
                    .license(license)
                    .logType(LicenseLogType.EXPIRED)
                    .data(Map.of("expiredLoggedAt", license.getExpiredAt().toString()))
                    .operatedAt(license.getExpiredAt())
                    .build();
            license.setExpiredLoggedAt();
            licenseLogs.add(licenseLog);
        });

        logRepository.saveAll(licenseLogs);
    }
}
