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
import koza.licensemanagementservice.verification.entity.ReleaseType;
import koza.licensemanagementservice.verification.entity.SessionLog;
import koza.licensemanagementservice.verification.repository.SessionLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationService {
    private final LicenseRepository licenseRepository;
    private final SessionManager sessionManager;
    private final SessionLogRepository sessionLogRepository;

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

        String sessionId = sessionManager.createSession(license.getId(), license.getExpiredAt());

        license.verify(sessionId);

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(now, license.getExpiredAt());
        long remainMs = Math.max(0, duration.toMillis());

        return VerifyResponse.builder()
                .sessionId(sessionId)
                .exp(license.getExpiredAt())
                .serverTime(LocalDateTime.now())
                .remainMs(remainMs)
                .localVariables(license.getLocalVariables())
                .build();
    }

    public HeartbeatResponse heartbeat(HeartbeatRequest request) {
        String sessionId = request.getSessionId();
        sessionManager.extendSession(sessionId); // 선 연장, 후 시간계산 -> 시간 계산 후 만료되는 것 방지
        SessionValue sessionValue = sessionManager.getSessionValue(sessionId);

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(now, sessionValue.getExpiredAt());
        long remainMs = Math.max(0, duration.toMillis());
        return new HeartbeatResponse(sessionValue.getExpiredAt(), remainMs);
    }

    @Transactional
    public void release(ReleaseRequest request) {
        String sessionId = request.getSessionId();
        SessionValue sessionValue = sessionManager.getSessionValue(sessionId);
        processRelease(sessionId);
        saveLog(sessionId, sessionValue, ReleaseType.NORMAL);
    }


    @Transactional
    public void revokeExpire(String sessionId) { // 만료된 세션 처리
        SessionValue sessionValue = sessionManager.getSessionValue(sessionId);
        processRelease(sessionId);
        saveLog(sessionId, sessionValue, ReleaseType.TIMEOUT);
    }

    private void processRelease(String sessionId) {
        SessionValue sessionValue = sessionManager.getSessionValue(sessionId);
        License license = licenseRepository.findById(sessionValue.getLicenseId()).orElseGet(() -> {
            log.warn("세션에 저장된 라이센스 ID가 잘못됐습니다. SessionId: {}", sessionId);
            return null;
        });
        if (license == null) return;

        license.release();
        sessionManager.releaseSession(sessionId);
    }

    private void saveLog(String sessionId, SessionValue sessionValue, ReleaseType releaseType) {
        License proxyLicense = licenseRepository.getReferenceById(sessionValue.getLicenseId()); // id 제외한 거 불러오면 X
        SessionLog log = SessionLog.builder()
                .license(proxyLicense)
                .sessionId(sessionId)
                .verifyAt(sessionValue.getVerifyAt())
                .releaseAt(LocalDateTime.now())
                .releaseType(releaseType)
                .build();
        sessionLogRepository.save(log);
    }
}
