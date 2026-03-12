package koza.licensemanagementservice.verification.service;

import jakarta.servlet.http.HttpServletRequest;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import koza.licensemanagementservice.domain.license.entity.License;
import koza.licensemanagementservice.domain.license.repository.LicenseRepository;
import koza.licensemanagementservice.domain.session.dto.SessionValue;
import koza.licensemanagementservice.domain.session.service.SessionManager;
import koza.licensemanagementservice.verification.dto.request.HeartbeatRequest;
import koza.licensemanagementservice.verification.dto.request.ReleaseRequest;
import koza.licensemanagementservice.verification.dto.request.VerifyRequest;
import koza.licensemanagementservice.verification.dto.resposne.HeartbeatResponse;
import koza.licensemanagementservice.verification.dto.resposne.VerifyResponse;
import koza.licensemanagementservice.domain.sessionLog.entity.ReleaseType;
import koza.licensemanagementservice.domain.sessionLog.repository.SessionLogRepository;
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
    public VerifyResponse verify(VerifyRequest request, HttpServletRequest servletRequest) {
        String licenseKey = request.getLicenseKey();
        License license = licenseRepository.findByLicenseKeyWithSoftware(licenseKey)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_LICENSE));

        // 라이센스 만료 시
        if (license.getExpiredAt().isBefore(LocalDateTime.now()))
            throw new BusinessException(ErrorCode.EXPIRED_LICENSE);

        String currentSessionId = sessionManager.getSessionIdByLicenseId(license.getId());

        // 사용중인 라이센스인 경우 연결 거부
        if (currentSessionId != null && sessionManager.isActive(currentSessionId))
            throw new BusinessException(ErrorCode.ALREADY_USE_LICENSE);

        String ipAddress = parseIpAddress(servletRequest);
        String userAgent = servletRequest.getHeader("User-Agent");
        String sessionId = sessionManager.createSession(license.getId(), ipAddress, userAgent, license.getExpiredAt());

        license.verify();

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(now, license.getExpiredAt());
        long remainMs = Math.max(0, duration.toMillis());

        return VerifyResponse.builder()
                .sessionId(sessionId)
                .exp(license.getExpiredAt())
                .serverTime(LocalDateTime.now())
                .remainMs(remainMs)
                .localVariables(license.getMergeLocalVariables())
                .globalVariables(license.getSoftware().getGlobalVariables())
                .build();
    }

    public HeartbeatResponse heartbeat(HeartbeatRequest request) {
        String sessionId = request.getSessionId();
        sessionManager.extendSession(sessionId); // 선 연장, 후 시간계산 -> 시간 계산 후 만료되는 것 방지
        SessionValue sessionValue = sessionManager.getSession(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXPIRED_SESSION));

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(now, sessionValue.getExpiredAt());
        long remainMs = Math.max(0, duration.toMillis());
        return new HeartbeatResponse(sessionValue.getExpiredAt(), remainMs);
    }

    @Transactional
    public void release(ReleaseRequest request) {
        String sessionId = request.getSessionId();
        SessionValue sessionValue = sessionManager.getSession(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXPIRED_SESSION));
        processRelease(sessionId, sessionValue.getLicenseId(), ReleaseType.NORMAL);
    }


    @Transactional
    public void revokeExpire(String sessionId) { // 만료된 세션 처리
        SessionValue sessionValue = sessionManager.getSession(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXPIRED_SESSION));
        processRelease(sessionId, sessionValue.getLicenseId(), ReleaseType.TIMEOUT);
    }

    private void processRelease(String sessionId, Long licenseId, ReleaseType releaseType) {
        License license = licenseRepository.findById(licenseId).orElseGet(() -> {
            log.warn("세션에 저장된 라이센스 ID가 잘못됐습니다. SessionId: {}", sessionId);
            return null;
        });
        if (license == null) return;

        license.release();
        sessionManager.releaseSession(sessionId, license, releaseType);
    }

    private String parseIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr(); // 기본 IP 가져오기
        }
        return ip;
    }
}
