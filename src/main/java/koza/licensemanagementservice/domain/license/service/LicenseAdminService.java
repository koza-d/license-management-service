package koza.licensemanagementservice.domain.license.service;

import koza.licensemanagementservice.auth.dto.user.CustomUser;
import koza.licensemanagementservice.domain.license.dto.request.AdminLicenseExtendRequest;
import koza.licensemanagementservice.domain.license.dto.request.LicenseStatusUpdateRequest;
import koza.licensemanagementservice.domain.license.dto.response.AdminLicenseDetailResponse;
import koza.licensemanagementservice.domain.license.dto.response.AdminLicenseExtendResponse;
import koza.licensemanagementservice.domain.license.dto.response.AdminLicenseSummaryResponse;
import koza.licensemanagementservice.domain.license.entity.License;
import koza.licensemanagementservice.domain.license.entity.LicenseStatus;
import koza.licensemanagementservice.domain.license.log.dto.event.LicenseAdminStatusChangedEvent;
import koza.licensemanagementservice.domain.license.log.dto.event.LicenseExtendEvent;
import koza.licensemanagementservice.domain.license.log.dto.event.LicenseStatusChangedEvent;
import koza.licensemanagementservice.domain.license.repository.LicenseRepository;
import koza.licensemanagementservice.domain.license.dto.condition.LicenseSearchCondition;
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
import java.util.List;
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
            LicenseStatus status = request.getStatus();
            LicenseStatus beforeStatus = target.getStatus();
            target.changeStatus(status);
            eventPublisher.publishEvent(new LicenseStatusChangedEvent(licenseId, user.getId(), beforeStatus, status, request.getReason(), LocalDateTime.now()));
            eventPublisher.publishEvent(new LicenseAdminStatusChangedEvent(licenseId, user.getId(), beforeStatus, status, request.getReason()));
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
    }

    @Transactional(readOnly = true)
    public Page<AdminLicenseSummaryResponse> getLicenseSummaryAll(CustomUser user, LicenseSearchCondition condition, Pageable pageable) {
        validAdminAuthorized(user);

        return licenseRepository.findByAllCondition(condition, pageable);
    }

    @Transactional(readOnly = true)
    public AdminLicenseDetailResponse getLicenseDetail(CustomUser user, Long licenseId) {
        validAdminAuthorized(user);

        License license = licenseRepository.findByIdWithSoftwareAndMember(licenseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_LICENSE));

        Map<String, Object> finalVars = license.getMergeLocalVariables();

        Optional<SessionValue> sessionOptional = sessionManager.getSessionByLicenseId(licenseId);
        LocalDateTime latestActiveAt = license.getLatestActiveAt();
        if (sessionOptional.isPresent())
            latestActiveAt = sessionOptional.get().getLatestActiveAt();

        // LicenseAdminDetailResponse.of 내부에서 license.software.versions 을 타고 들어가서 쿼리 1번이 더 나감
        return AdminLicenseDetailResponse.of(license, latestActiveAt, finalVars);
    }

    @Transactional
    public AdminLicenseExtendResponse extend(CustomUser user, Long licenseId, AdminLicenseExtendRequest request) {
        validAdminAuthorized(user);

        License license = licenseRepository.findById(licenseId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_LICENSE));
        LocalDateTime beforeExpiredAt = license.getExpiredAt();
        license.extendPeriod(request.getDays());

        Long periodMs = request.getDays() * 24 * 60 * 60 * 1000L;
        eventPublisher.publishEvent(new LicenseExtendEvent(user.getId(), licenseId, beforeExpiredAt, license.getExpiredAt(), periodMs));
        return AdminLicenseExtendResponse.of(license, request.getDays());
    }

    /**
     * 라이센스 만료기간이 지난 상태를 EXPIRED 로 변경하는 메서드
     * - 스케줄러에서만 호출, 임의 호출 금지
     */
    @Transactional
    public void updateExpiredLicenseStatus() {
        LocalDateTime now = LocalDateTime.now();
        List<License> updateLicenses = licenseRepository.bulkUpdateExpiredStatus(now);
        updateLicenses.forEach(license -> {
            eventPublisher.publishEvent(new LicenseStatusChangedEvent(
                    license.getId(), 0L,
                    LicenseStatus.ACTIVE, LicenseStatus.EXPIRED,
                    "[스케줄러] 라이센스 만료로 인해 상태 변경",
                    license.getExpiredAt()));
        });
    }
}
