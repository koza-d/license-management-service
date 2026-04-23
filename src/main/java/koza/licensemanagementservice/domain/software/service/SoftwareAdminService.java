package koza.licensemanagementservice.domain.software.service;

import koza.licensemanagementservice.auth.dto.CustomUser;
import koza.licensemanagementservice.domain.license.dto.response.AdminLicenseStatResponse;
import koza.licensemanagementservice.domain.license.entity.LicenseStatus;
import koza.licensemanagementservice.domain.license.repository.LicenseRepository;
import koza.licensemanagementservice.domain.software.dto.request.SoftwareBanRequest;
import koza.licensemanagementservice.domain.software.dto.request.SoftwareUnbanRequest;
import koza.licensemanagementservice.domain.software.dto.response.SoftwareAdminDetailResponse;
import koza.licensemanagementservice.domain.software.dto.response.SoftwareAdminStatsResponse;
import koza.licensemanagementservice.domain.software.dto.response.SoftwareAdminSummaryResponse;
import koza.licensemanagementservice.domain.software.entity.SoftwareStatus;
import koza.licensemanagementservice.domain.software.log.dto.AdminSoftwareStatusChangedEvent;
import koza.licensemanagementservice.domain.software.entity.Software;
import koza.licensemanagementservice.domain.software.repository.SoftwareAdminSearchCondition;
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

import static koza.licensemanagementservice.global.validation.ValidUserAuthorized.validAdminAuthorized;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SoftwareAdminService {
    private final SoftwareRepository softwareRepository;
    private final LicenseRepository licenseRepository;

    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void ban(CustomUser user, Long softwareId, SoftwareBanRequest request) {
        validAdminAuthorized(user);

        Software software = softwareRepository.findById(softwareId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SOFTWARE_NOT_FOUND));

        SoftwareStatus beforeStatus = software.getStatus();
        if (beforeStatus == SoftwareStatus.BANNED) // 이미 밴된 상태인 경우
            throw new BusinessException(ErrorCode.SOFTWARE_BANNED);

        LocalDateTime banUntil = request.getUntilDays() == 0 ? null :
                LocalDateTime.now().plusDays(request.getUntilDays());
        String reason = request.getReason();

        software.changeStatus(SoftwareStatus.BANNED, banUntil);
        eventPublisher.publishEvent(new AdminSoftwareStatusChangedEvent(softwareId, user.getId(), beforeStatus, SoftwareStatus.BANNED, banUntil, reason));
    }

    @Transactional
    public void unban(CustomUser user, Long softwareId, SoftwareUnbanRequest request) {
        validAdminAuthorized(user);

        Software software = softwareRepository.findById(softwareId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SOFTWARE_NOT_FOUND));

        String reason = request.getReason();
        SoftwareStatus status = software.getStatus();
        if (status != SoftwareStatus.BANNED)
            throw new BusinessException(ErrorCode.SOFTWARE_NOT_BANNED);

        software.changeStatus(SoftwareStatus.INACTIVE);
        eventPublisher.publishEvent(new AdminSoftwareStatusChangedEvent(softwareId, user.getId(), SoftwareStatus.BANNED, SoftwareStatus.INACTIVE, reason));
    }

    @Transactional(readOnly = true)
    public Page<SoftwareAdminSummaryResponse> getSoftwareList(CustomUser user, SoftwareAdminSearchCondition condition, Pageable pageable) {
        validAdminAuthorized(user);

        return softwareRepository.searchSoftwareByCondition(condition, pageable);
    }

    @Transactional(readOnly = true)
    public SoftwareAdminDetailResponse getSoftwareDetail(CustomUser user, Long softwareId) {
        validAdminAuthorized(user);
        return softwareRepository.findBySoftwareId(softwareId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public SoftwareAdminStatsResponse getSoftwareStats(CustomUser user, Long softwareId) {
        validAdminAuthorized(user);
        return softwareRepository.getSoftwareUsageStat(softwareId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public AdminLicenseStatResponse getLicenseStat(CustomUser user, Long softwareId) {
        validAdminAuthorized(user);

        softwareRepository.findById(softwareId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        return AdminLicenseStatResponse.builder()
                .total((long) licenseRepository.countBySoftwareId(softwareId))
                .expire(licenseRepository.countBySoftwareIdAndExpiredAtBefore(softwareId, LocalDateTime.now()))
                .active(licenseRepository.countBySoftwareIdAndStatusEquals(softwareId, LicenseStatus.ACTIVE))
                .banned(licenseRepository.countBySoftwareIdAndStatusEquals(softwareId, LicenseStatus.BANNED))
                .activeSessions(licenseRepository.countBySoftwareIdAndHasActiveSessionTrue(softwareId))
                .build();
    }
}
