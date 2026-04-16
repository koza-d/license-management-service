package koza.licensemanagementservice.domain.license.service;

import koza.licensemanagementservice.auth.dto.CustomUser;
import koza.licensemanagementservice.domain.license.dto.request.LicenseAdminExtendRequest;
import koza.licensemanagementservice.domain.license.dto.request.LicenseStatusUpdateRequest;
import koza.licensemanagementservice.domain.license.dto.response.LicenseAdminDetailResponse;
import koza.licensemanagementservice.domain.license.dto.response.LicenseAdminExtendResponse;
import koza.licensemanagementservice.domain.license.dto.response.LicenseAdminSummaryResponse;
import koza.licensemanagementservice.domain.license.dto.response.LicenseStat;
import koza.licensemanagementservice.domain.license.entity.License;
import koza.licensemanagementservice.domain.license.entity.LicenseStatus;
import koza.licensemanagementservice.domain.license.log.dto.LicenseExtendEvent;
import koza.licensemanagementservice.domain.license.log.dto.LicenseStatusChangedEvent;
import koza.licensemanagementservice.domain.license.repository.LicenseRepository;
import koza.licensemanagementservice.domain.license.repository.condition.LicenseSearchCondition;
import koza.licensemanagementservice.domain.session.dto.SessionValue;
import koza.licensemanagementservice.domain.session.service.SessionManager;
import koza.licensemanagementservice.domain.software.repository.SoftwareRepository;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static koza.licensemanagementservice.global.validation.ValidUserAuthorized.validAdminAuthorized;

@Service
@RequiredArgsConstructor
public class LicenseAdminService {
    private final SoftwareRepository softwareRepository;
    private final LicenseRepository licenseRepository;
    private final SessionManager sessionManager;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void changeStatus(CustomUser user, Long licenseId, LicenseStatusUpdateRequest request) {
        validAdminAuthorized(user);

        License target = licenseRepository.findById(licenseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_LICENSE));
        try {
            LicenseStatus status = LicenseStatus.valueOf(request.getStatus());
            LicenseStatus beforeStatus = target.getStatus();
            target.changeStatus(status);
            eventPublisher.publishEvent(new LicenseStatusChangedEvent(licenseId, user.getId(), beforeStatus, status, request.getReason()));
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
    }

    @Transactional(readOnly = true)
    public Page<LicenseAdminSummaryResponse> getLicenseSummaryAll(CustomUser user, LicenseSearchCondition condition, Pageable pageable) {
        validAdminAuthorized(user);

        return licenseRepository.findByAllCondition(condition, pageable);
    }

    @Transactional(readOnly = true)
    public LicenseAdminDetailResponse getLicenseDetail(CustomUser user, Long licenseId) {
        validAdminAuthorized(user);

        License license = licenseRepository.findByIdWithSoftwareAndMember(licenseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_LICENSE));

        Map<String, Object> finalVars = license.getMergeLocalVariables();

        Optional<SessionValue> sessionOptional = sessionManager.getSessionByLicenseId(licenseId);
        LocalDateTime latestActiveAt = license.getLatestActiveAt();
        if (sessionOptional.isPresent())
            latestActiveAt = sessionOptional.get().getLatestActiveAt();

        // LicenseAdminDetailResponse.of 내부에서 license.software.versions 을 타고 들어가서 쿼리 1번이 더 나감
        return LicenseAdminDetailResponse.of(license, latestActiveAt, finalVars);
    }

    @Transactional
    public LicenseAdminExtendResponse extend(CustomUser user, Long licenseId, LicenseAdminExtendRequest request) {
        validAdminAuthorized(user);

        License license = licenseRepository.findById(licenseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_LICENSE));
        LocalDateTime beforeExpiredAt = license.getExpiredAt();
        license.extendPeriod(request.getDays());

        Long periodMs = request.getDays() * 24 * 60 * 60 * 1000L;
        eventPublisher.publishEvent(new LicenseExtendEvent(user.getId(), licenseId, beforeExpiredAt, license.getExpiredAt(), periodMs));
        return LicenseAdminExtendResponse.of(license, request.getDays());
    }

    @Transactional(readOnly = true)
    public LicenseStat getLicenseStatBySoftware(CustomUser user, Long softwareId) {
        validAdminAuthorized(user);

        softwareRepository.findById(softwareId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        return LicenseStat.builder()
                .total((long) licenseRepository.countBySoftwareId(softwareId))
                .expire(licenseRepository.countBySoftwareIdAndExpiredAtBefore(softwareId, LocalDateTime.now()))
                .active(licenseRepository.countBySoftwareIdAndStatusEquals(softwareId, LicenseStatus.ACTIVE))
                .banned(licenseRepository.countBySoftwareIdAndStatusEquals(softwareId, LicenseStatus.BANNED))
                .activeSessions(licenseRepository.countBySoftwareIdAndHasActiveSessionTrue(softwareId))
                .build();
    }
}
