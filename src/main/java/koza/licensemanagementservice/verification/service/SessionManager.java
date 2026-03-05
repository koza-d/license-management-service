package koza.licensemanagementservice.verification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import koza.licensemanagementservice.license.entity.License;
import koza.licensemanagementservice.license.repository.LicenseRepository;
import koza.licensemanagementservice.verification.dto.SessionValue;
import koza.licensemanagementservice.verification.entity.ReleaseType;
import koza.licensemanagementservice.verification.entity.SessionLog;
import koza.licensemanagementservice.verification.repository.SessionLogRepository;
import koza.licensemanagementservice.verification.repository.SessionRepository;
import koza.licensemanagementservice.verification.status.SessionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

/*
 * 세션 관리 방식
 * 준비) Trigger 키 (TTL 만료 이벤트 용), 세션 키 (실 데이터 저장 용)
 * 1) 세션 등록 시 : Trigger 키(TTL 부여), 세션 키(영구 TTL) 저장
 * 2) 세션 정상 /release 시 : Trigger 키, 세션 키 같이 제거
 * 3) 세션 TTL 만료 시 : Trigger 키 만료 이벤트 수신 -> 세션 키 제거
 */
@Component
@RequiredArgsConstructor
public class SessionManager {
    private final SessionRepository sessionRepository;

    private final Duration SESSION_TTL = Duration.of(60, ChronoUnit.SECONDS);

    public String createSession(Long licenseId, LocalDateTime expiredAt) {
        String sessionId = createNewSessionId();
        SessionValue sessionValue = SessionValue.builder()
                .licenseId(licenseId)
                .expiredAt(expiredAt)
                .verifyAt(LocalDateTime.now())
                .build();
        sessionRepository.save(sessionId, sessionValue, SESSION_TTL);
        return sessionId;
    }

    public SessionValue getSessionValue(String sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EXPIRED_SESSION));
    }

    public void extendSession(String sessionId) {
        boolean suc = sessionRepository.extendTTL(sessionId, SESSION_TTL);
        if (!suc)
            throw new BusinessException(ErrorCode.EXPIRED_SESSION);
    }


    public SessionStatus getStatus(String sessionId) {
        return isActive(sessionId) ? SessionStatus.CONNECTED : SessionStatus.DISCONNECTED;
    }

    public boolean isActive(String sessionId) {
        return sessionRepository.hasSession(sessionId);
    }

    public void releaseSession(String sessionId) {
        sessionRepository.delete(sessionId);
    }

    private String createNewSessionId() {
        return UUID.randomUUID().toString();
    }
}
