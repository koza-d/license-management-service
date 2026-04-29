package koza.licensemanagementservice.sdk.log.service;

import koza.licensemanagementservice.domain.license.repository.LicenseRepository;
import koza.licensemanagementservice.domain.software.repository.SoftwareRepository;
import koza.licensemanagementservice.sdk.log.dto.VerifyFailedEvent;
import koza.licensemanagementservice.sdk.log.dto.VerifySuccessEvent;
import koza.licensemanagementservice.sdk.log.entity.SdkLog;
import koza.licensemanagementservice.sdk.log.repository.SdkLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class SdkLogListener {
    private final SdkLogRepository sdkLogRepository;
    private final LicenseRepository licenseRepository;
    private final SoftwareRepository softwareRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleVerifySuccess(VerifySuccessEvent event) {
        SdkLog sdkLog = SdkLog.builder()
                .isSuccess(true)
                .software(softwareRepository.getReferenceById(event.getSoftwareId()))
                .appId(event.getAppId())
                .license(licenseRepository.getReferenceById(event.getLicenseId()))
                .licenseKey(event.getLicenseKey())
                .failCode(null)
                .ipAddress(event.getIpAddress())
                .userAgent(event.getUserAgent())
                .build();
        sdkLogRepository.save(sdkLog);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleVerifyFailed(VerifyFailedEvent event) {
        SdkLog sdkLog = SdkLog.builder()
                .isSuccess(false)
                .software(event.getSoftwareId() != null ? softwareRepository.getReferenceById(event.getSoftwareId()) : null)
                .appId(event.getAppId())
                .license(event.getLicenseId() != null ? licenseRepository.getReferenceById(event.getLicenseId()) : null)
                .licenseKey(event.getLicenseKey())
                .failCode(event.getFailCode())
                .ipAddress(event.getIpAddress())
                .userAgent(event.getUserAgent())
                .build();
        sdkLogRepository.save(sdkLog);
    }

}
