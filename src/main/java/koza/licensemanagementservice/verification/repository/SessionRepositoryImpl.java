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
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SessionRepositoryImpl implements SessionRepository {
    public static final String SESSION_KEY_PREFIX = "session";
    public static final String SESSION_TRIGGER_PREFIX = "trigger";

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public void save(String sessionId, SessionValue sessionValue, Duration ttl) {
        String value = toJson(sessionValue);
        String sessionKey = String.format("%s:%s", SESSION_KEY_PREFIX, sessionId);
        String triggerKey = String.format("%s:%s", SESSION_TRIGGER_PREFIX, sessionId);
        redisTemplate.opsForValue().set(sessionKey, value);
        redisTemplate.opsForValue().set(triggerKey, "", ttl);
    }

    public Optional<SessionValue> findById(String sessionId) {
        String sessionKey = String.format("%s:%s", SESSION_KEY_PREFIX, sessionId);
        String json = redisTemplate.opsForValue().get(sessionKey);
        return Optional.ofNullable(json).map(this::fromJson);
    }

    public boolean hasSession(String sessionId) {
        String triggerKey = String.format("%s:%s", SESSION_TRIGGER_PREFIX, sessionId);
        return Boolean.TRUE.equals(redisTemplate.hasKey(triggerKey));
    }

    public boolean extendTTL(String sessionId, Duration ttl) {
        String triggerKey = String.format("%s:%s", SESSION_TRIGGER_PREFIX, sessionId);
        Boolean expire = redisTemplate.expire(triggerKey, ttl);
        return Boolean.TRUE.equals(expire);
    }

    public void delete(String sessionId) {
        String sessionKey = String.format("%s:%s", SESSION_KEY_PREFIX, sessionId);
        String triggerKey = String.format("%s:%s", SESSION_TRIGGER_PREFIX, sessionId);
        redisTemplate.delete(List.of(sessionKey, triggerKey));
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
