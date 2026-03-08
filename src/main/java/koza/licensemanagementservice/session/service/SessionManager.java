package koza.licensemanagementservice.session.service;

import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import koza.licensemanagementservice.license.entity.License;
import koza.licensemanagementservice.session.dto.SessionValue;
import koza.licensemanagementservice.session.entity.ReleaseType;
import koza.licensemanagementservice.session.entity.SessionLog;
import koza.licensemanagementservice.session.repository.SessionLogRepository;
import koza.licensemanagementservice.session.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

/*
 * 세션 관리 방식
 * 준비) Trigger 키 (TTL 만료 이벤트 용), 세션 키 (실 데이터 저장 용), License 키(sessionId 역참조용)
 * 1) 세션 등록 시 : Trigger 키(TTL 부여), 세션 키, License 키(영구 TTL) 저장
 * 2) 세션 정상 /release 시 : Trigger 키, 세션 키, License 키 같이 제거
 * 3) 세션 TTL 만료 시 : Trigger 키 만료 이벤트 수신 -> 세션 키, License 키 제거
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SessionManager {
    private final SessionRepository sessionRepository;
    private final SessionLogRepository logRepository;

    private final Duration SESSION_TTL = Duration.of(60, ChronoUnit.SECONDS);

    public String createSession(Long licenseId, String ipAddress, String userAgent, LocalDateTime expiredAt) {
        String sessionId = createNewSessionId();
        SessionValue sessionValue = SessionValue.builder()
                .sessionId(sessionId)
                .licenseId(licenseId)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .expiredAt(expiredAt)
                .verifyAt(LocalDateTime.now())
                .latestActiveAt(LocalDateTime.now())
                .build();
        sessionRepository.save(sessionId, sessionValue, SESSION_TTL);
        return sessionId;
    }

    public Optional<SessionValue> getSessionByLicenseId(Long licenseId) {
        String sessionId = sessionRepository.findSessionIdByLicenseId(licenseId);
        if (sessionId == null)
            return Optional.empty();

        return sessionRepository.findById(sessionId);
    }

    public Optional<SessionValue> getSession(String sessionId) {
        return sessionRepository.findById(sessionId);
    }

    public String getSessionIdByLicenseId(Long licenseId) {
        return sessionRepository.findSessionIdByLicenseId(licenseId);
    }

    public void extendSession(String sessionId) {
        boolean suc = sessionRepository.extendTTL(sessionId, SESSION_TTL);
        if (!suc)
            throw new BusinessException(ErrorCode.EXPIRED_SESSION);
        else {
            SessionValue session = sessionRepository.findById(sessionId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.EXPIRED_SESSION));
            session.setLatestActiveAt(LocalDateTime.now());
            sessionRepository.save(sessionId, session, SESSION_TTL);
        }
    }

    public boolean isActive(String sessionId) {
        return sessionRepository.hasSession(sessionId);
    }

    public void releaseSession(String sessionId, License license, ReleaseType releaseType) {
        SessionValue session = getSession(sessionId).orElse(null);
        if (session == null) {
            log.warn("세션 해제 중 세션을 찾을 수 없습니다. sessionId = {} ", sessionId);
            return;
        }

        sessionRepository.delete(session.getSessionId());
        SessionLog log = SessionLog.builder()
                .sessionId(session.getSessionId())
                .license(license)
                .verifyAt(session.getVerifyAt())
                .releaseAt(LocalDateTime.now())
                .releaseType(releaseType)
                .build();
        logRepository.save(log);
    }

    private String createNewSessionId() {
        return UUID.randomUUID().toString();
    }
}
