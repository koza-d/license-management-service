package koza.licensemanagementservice.verification.service;

import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import koza.licensemanagementservice.license.entity.License;
import koza.licensemanagementservice.license.repository.LicenseRepository;
import koza.licensemanagementservice.verification.dto.SessionValue;
import koza.licensemanagementservice.verification.dto.request.HeartbeatRequest;
import koza.licensemanagementservice.verification.dto.request.ReleaseRequest;
import koza.licensemanagementservice.verification.dto.request.VerifyRequest;
import koza.licensemanagementservice.verification.dto.resposne.HeartbeatResponse;
import koza.licensemanagementservice.verification.dto.resposne.VerifyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VerificationService {
    private final LicenseRepository licenseRepository;
    private final SessionManager sessionManager;
    @Transactional
    public VerifyResponse verify(VerifyRequest request) {
        String licenseKey = request.getLicenseKey();
        License license = licenseRepository.findByLicenseKey(licenseKey)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_LICENSE));

        // 라이센스 만료 시
        if (license.getExpiredAt().isBefore(LocalDateTime.now()))
            throw new BusinessException(ErrorCode.EXPIRED_LICENSE);

        String currentSessionId = license.getCurrentSessionId();

        // 사용중인 라이센스인 경우 연결 거부
        if (currentSessionId != null && sessionManager.isActive(currentSessionId))
            throw new BusinessException(ErrorCode.ALREADY_USE_LICENSE);

        String sessionId = sessionManager.createSession(licenseKey, license.getExpiredAt());

        license.verify(sessionId);

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(now, license.getExpiredAt());
        long remainMs = Math.max(0, duration.toMillis());

        return VerifyResponse.builder()
                .sessionId(sessionId)
                .exp(license.getExpiredAt())
                .serverTime(LocalDateTime.now())
                .remainMs(remainMs)
                .metadata(license.getMetadata())
                .build();
    }

    public HeartbeatResponse heartbeat(HeartbeatRequest request) {
        String sessionId = request.getSessionId();
        SessionValue sessionValue = sessionManager.getSessionValue(sessionId);

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(now, sessionValue.getExpiredAt());
        long remainMs = Math.max(0, duration.toMillis());

        sessionManager.extendSession(sessionId);
        return new HeartbeatResponse(sessionValue.getExpiredAt(), remainMs);
    }

}
