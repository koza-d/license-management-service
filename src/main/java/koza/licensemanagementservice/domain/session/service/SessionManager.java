package koza.licensemanagementservice.domain.session.service;

import koza.licensemanagementservice.domain.license.repository.LicenseRepository;
import koza.licensemanagementservice.domain.session.repository.SessionRepository;
import koza.licensemanagementservice.domain.session.log.entity.SessionLog;
import koza.licensemanagementservice.domain.session.log.repository.SessionLogRepository;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import koza.licensemanagementservice.domain.license.entity.License;
import koza.licensemanagementservice.domain.session.dto.SessionValue;
import koza.licensemanagementservice.domain.session.log.entity.ReleaseType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
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
    private static final Duration SESSION_TTL = Duration.of(60, ChronoUnit.SECONDS);
    // 세션 TTL(60s) + 유예(60s). latestActiveAt 이 이보다 오래되면 좀비로 간주
    private static final Long GHOST_ACTIVE_THRESHOLD = 120 * 1000L;

    private final SessionRepository sessionRepository;
    private final SessionLogRepository logRepository;
    private final LicenseRepository licenseRepository;


    public String createSession(License license, String ipAddress, String userAgent, LocalDateTime expiredAt, byte[] keyC2S, byte[] keyS2C) {
        Optional<SessionValue> sessionByLicenseId = getSessionByLicenseId(license.getId());
        sessionByLicenseId.ifPresent(sessionValue -> releaseSession(sessionValue.getSessionId(), license, ReleaseType.REPLACED));

        String sessionId = createNewSessionId();
        SessionValue sessionValue = SessionValue.builder()
                .sessionId(sessionId)
                .licenseId(license.getId())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .expiredAt(expiredAt == null
                        ? LocalDateTime.now().plusDays(license.getStartDurationDays())
                        : expiredAt
                )
                .verifyAt(LocalDateTime.now())
                .latestActiveAt(LocalDateTime.now())
                .keyC2S(keyC2S)
                .keyS2C(keyS2C)
                .build();
        sessionRepository.save(sessionId, sessionValue, SESSION_TTL);
        return sessionId;
    }

    public Optional<SessionValue> getSessionByLicenseId(Long licenseId) {
        return sessionRepository.findSessionByLicenseId(licenseId);
    }

    public List<SessionValue> getSessionsByLicenseIds(List<Long> licenseIds) {
        return sessionRepository.findSessionsByLicenseIds(licenseIds);
    }

    public Optional<SessionValue> getSession(String sessionId) {
        return sessionRepository.findById(sessionId);
    }

    public Long getSequence(String sessionId) {
        return sessionRepository.findSequenceById(sessionId);
    }

    public Long extendSession(SessionValue session) {
        session.setLatestActiveAt(LocalDateTime.now());
        sessionRepository.update(session.getSessionId(), session, SESSION_TTL);
        return sessionRepository.increaseSequence(session.getSessionId());
    }

    public void updateSession(String sessionId, SessionValue sessionValue) {
        sessionRepository.update(sessionId, sessionValue, SESSION_TTL);
    }

    public void releaseSession(String sessionId, License license, ReleaseType releaseType) {
        SessionValue session = getSession(sessionId).orElse(null);
        if (session == null) {
            log.warn("세션 해제 중 세션을 찾을 수 없습니다. sessionId = {} ", sessionId);
            return;
        }
        license.release(session.getChangedLocalVariables());
        sessionRepository.delete(session.getSessionId(), license.getId());
        LocalDateTime releaseAt = LocalDateTime.now();
        LocalDateTime latestActiveAt = session.getLatestActiveAt();
        boolean isOld = latestActiveAt != null && Duration.between(latestActiveAt, LocalDateTime.now()).toMillis() >= GHOST_ACTIVE_THRESHOLD;
        // 마지막 활동이 오래된 경우 or 정상적인 상황이 아닌 경우 마지막 활동시간을 releaseAt 으로 지정
        if (releaseType == ReleaseType.SYSTEM_ERROR || isOld)
            releaseAt = latestActiveAt;

        // 이 경우는 없을거로 예상되지만 혹시모를 null 안전장치
        if (latestActiveAt == null)
            releaseAt = LocalDateTime.now();

        SessionLog log = SessionLog.builder()
                .sessionId(session.getSessionId())
                .license(license)
                .ipAddress(session.getIpAddress())
                .userAgent(session.getUserAgent())
                .verifyAt(session.getVerifyAt())
                .releaseAt(releaseAt)
                .releaseType(releaseType)
                .build();
        logRepository.save(log);
    }

    @Transactional
    public void cleanUpGhostSession(Long licenseId) {
        License license = licenseRepository.findById(licenseId).orElse(null);
        if (license == null || !license.hasActiveSession())
            return;

        Optional<SessionValue> sessionOpt = getSessionByLicenseId(licenseId);
        if (sessionOpt.isEmpty()) {
            // Redis 에 살아있는 세션 없음 -> DB 플래그만 잔존(키 즉시삭제 후 커밋 실패 등)
            sessionRepository.deleteByLicenseId(licenseId);
            license.release(Map.of());
            log.warn("유령 세션 정리(Redis 세션 없음). licenseId = {}", licenseId);
            return;
        }

        SessionValue session = sessionOpt.get();
        LocalDateTime latestActiveAt = session.getLatestActiveAt();
        boolean isGhost = latestActiveAt == null || Duration.between(latestActiveAt, LocalDateTime.now()).toMillis() >= GHOST_ACTIVE_THRESHOLD;
        if (isGhost) {
            // Redis 키 잔존 + 마지막 활동이 임계치 초과 -> TTL 만료 이벤트 미처리/미만료 좀비
            releaseSession(session.getSessionId(), license, ReleaseType.SYSTEM_ERROR);
            log.warn("유령 세션 정리(old active). licenseId = {}, sessionId = {}, latestActiveAt = {}",
                    licenseId, session.getSessionId(), latestActiveAt);
        }
    }

    private String createNewSessionId() {
        return UUID.randomUUID().toString();
    }
}
