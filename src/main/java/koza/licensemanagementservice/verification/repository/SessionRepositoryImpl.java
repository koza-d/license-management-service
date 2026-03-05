package koza.licensemanagementservice.verification.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import koza.licensemanagementservice.verification.dto.SessionValue;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class SessionRepositoryImpl implements SessionRepository {
    public static final String SESSION_KEY_PREFIX = "session";
    public static final String SESSION_TRIGGER_PREFIX = "trigger";

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public void save(String sessionId, SessionValue sessionValue, Duration ttl) {
        String value = toJson(sessionValue);
        String sessionKey = getSessionKeyFormat(sessionId);
        String triggerKey = getTriggerKeyFormat(sessionId);
        redisTemplate.opsForValue().set(sessionKey, value);
        redisTemplate.opsForValue().set(triggerKey, "", ttl);
    }

    public Optional<SessionValue> findById(String sessionId) {
        String sessionKey = getSessionKeyFormat(sessionId);
        String json = redisTemplate.opsForValue().get(sessionKey);
        return Optional.ofNullable(json).map(this::fromJson);
    }

    public Optional<LocalDateTime> findLatestActiveAtByIdAndTTL(String sessionId, Duration ttl) {
        if (sessionId == null || sessionId.isEmpty())
            return Optional.empty();

        Long remainExpire = redisTemplate.getExpire(getTriggerKeyFormat(sessionId), TimeUnit.MILLISECONDS);
        if (remainExpire == null || remainExpire == -2) // Redis의 예상치못한 오류거나 키가 없는경우
            return Optional.empty();

        long activeDurationMillis = ttl.toMillis() - remainExpire; // 마지막 활성화에서 경과된 ms
        LocalDateTime latestActiveAt = LocalDateTime.now().minus(activeDurationMillis, ChronoUnit.MILLIS);
        return Optional.of(latestActiveAt);
    }

    public boolean hasSession(String sessionId) {
        String triggerKey = getTriggerKeyFormat(sessionId);
        return Boolean.TRUE.equals(redisTemplate.hasKey(triggerKey));
    }

    public boolean extendTTL(String sessionId, Duration ttl) {
        String triggerKey = getTriggerKeyFormat(sessionId);
        Boolean expire = redisTemplate.expire(triggerKey, ttl);
        return Boolean.TRUE.equals(expire);
    }

    public void delete(String sessionId) {
        String sessionKey = getSessionKeyFormat(sessionId);
        String triggerKey = getTriggerKeyFormat(sessionId);
        redisTemplate.delete(List.of(sessionKey, triggerKey));
    }

    private String getSessionKeyFormat(String sessionId) {
        return String.format("%s:%s", SESSION_KEY_PREFIX, sessionId);
    }

    private String getTriggerKeyFormat(String sessionId) {
        return String.format("%s:%s", SESSION_TRIGGER_PREFIX, sessionId);
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
