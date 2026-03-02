package koza.licensemanagementservice.verification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import koza.licensemanagementservice.verification.dto.SessionValue;
import koza.licensemanagementservice.verification.status.SessionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class SessionManager {
    private final RedisTemplate<String, Object> redisTemplate;
    private final Duration SESSION_TTL = Duration.of(60, ChronoUnit.SECONDS);
    private final ObjectMapper objectMapper;

    public SessionStatus getStatus(String sessionId) {
        return isActive(sessionId) ? SessionStatus.CONNECTED : SessionStatus.DISCONNECTED;
    }

    // Boolean.TRUE.equals -> hasKey null 반환 가능성 있음
    public boolean isActive(String sessionId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(sessionId));
    }

    public SessionValue getSessionValue(String sessionId) {
        Object data = redisTemplate.opsForValue().get(sessionId);
        return fromJson(Optional.ofNullable(redisTemplate.opsForValue().get(sessionId))
                .orElseThrow(() -> new BusinessException(ErrorCode.EXPIRED_SESSION)).toString());
    }

    public String createSession(String licenseKey, LocalDateTime expiredAt) {
        String sessionId = createNewSessionId();
        SessionValue sessionValue = SessionValue.builder()
                .licenseKey(licenseKey)
                .expiredAt(expiredAt)
                .verifyAt(LocalDateTime.now())
                .build();
        String value = toJson(sessionValue);
        redisTemplate.opsForValue().set(sessionId, value, SESSION_TTL);
        return sessionId;
    }

    public void extendSession(String sessionId) {
        // TTL 연장 실패(키가 사라진 경우) 시 false, 성공 시 true
        if (Boolean.FALSE.equals(redisTemplate.expire(sessionId, SESSION_TTL)))
            throw new BusinessException(ErrorCode.EXPIRED_SESSION);
    }

    public String releaseSession(String sessionId) {
        SessionValue sessionValue = getSessionValue(sessionId);
        redisTemplate.expire(sessionId, Duration.of(0, ChronoUnit.MILLIS));
        return sessionValue.getLicenseKey();
    }

    private String createNewSessionId() {
        return UUID.randomUUID().toString();
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private SessionValue fromJson(String json) {
        try {
            return objectMapper.readValue(json, SessionValue.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
