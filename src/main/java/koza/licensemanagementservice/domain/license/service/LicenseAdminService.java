package koza.licensemanagementservice.domain.license.service;

import koza.licensemanagementservice.auth.dto.CustomUser;
import koza.licensemanagementservice.domain.license.dto.request.LicenseStatusUpdateRequest;
import koza.licensemanagementservice.domain.license.entity.License;
import koza.licensemanagementservice.domain.license.entity.LicenseStatus;
import koza.licensemanagementservice.domain.license.log.dto.LicenseStatusChangedEvent;
import koza.licensemanagementservice.domain.license.repository.LicenseRepository;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LicenseAdminService {
    private final LicenseRepository licenseRepository;
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

    private static void validAdminAuthorized(CustomUser user) {
        user.getAuthorities().stream()
                .filter(auth -> auth.toString().equals("ADMIN"))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCESS_DENIED));
    }

}
