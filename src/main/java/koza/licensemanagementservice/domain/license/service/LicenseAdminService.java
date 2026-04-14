package koza.licensemanagementservice.domain.license.service;

import koza.licensemanagementservice.auth.dto.CustomUser;
import koza.licensemanagementservice.domain.license.dto.request.LicenseStatusUpdateRequest;
import koza.licensemanagementservice.domain.license.dto.response.LicenseAdminDetailResponse;
import koza.licensemanagementservice.domain.license.dto.response.LicenseAdminSummaryResponse;
import koza.licensemanagementservice.domain.license.entity.License;
import koza.licensemanagementservice.domain.license.entity.LicenseStatus;
import koza.licensemanagementservice.domain.license.log.dto.LicenseStatusChangedEvent;
import koza.licensemanagementservice.domain.license.repository.LicenseRepository;
import koza.licensemanagementservice.domain.license.repository.condition.LicenseSearchCondition;
import koza.licensemanagementservice.domain.session.dto.SessionValue;
import koza.licensemanagementservice.domain.session.service.SessionManager;
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

@Service
@RequiredArgsConstructor
public class LicenseAdminService {
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

    public Page<LicenseAdminSummaryResponse> getLicenseSummaryAll(CustomUser user, LicenseSearchCondition condition, Pageable pageable) {
        return licenseRepository.findByAllCondition(condition, pageable);
    }

    public LicenseAdminDetailResponse getLicenseDetail(CustomUser user, Long licenseId) {
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
    private static void validAdminAuthorized(CustomUser user) {
        user.getAuthorities().stream()
                .filter(auth -> auth.toString().equals("ADMIN"))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCESS_DENIED));
    }

}
