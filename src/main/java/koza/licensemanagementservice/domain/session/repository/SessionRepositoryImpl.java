package koza.licensemanagementservice.domain.session.repository;

import ch.qos.logback.core.util.StringUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import koza.licensemanagementservice.domain.session.dto.SessionValue;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SessionRepositoryImpl implements SessionRepository {
    public static final String SESSION_KEY_PREFIX = "session";
    public static final String SESSION_LICENSE_PREFIX = "license";
    public static final String SESSION_TRIGGER_PREFIX = "trigger";
    public static final String SESSION_SEQ_PREFIX = "seq";
    public static final String SESSION_LOCK_PREFIX = "lock";

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public void save(String sessionId, SessionValue sessionValue, Duration ttl) {
        String value = toJson(sessionValue);
        String sessionKey = getSessionKeyFormat(sessionId);
        String licenseKey = getLicenseKeyFormat(sessionValue.getLicenseId());
        String triggerKey = getTriggerKeyFormat(sessionId);
        String sequenceKey = getSequenceKeyFormat(sessionId);
        String lockKey = getLockKeyFormat(sessionValue.getLicenseId());

        Boolean isSave = redisTemplate.opsForValue().setIfAbsent(lockKey, sessionId, ttl);
        if (!Boolean.TRUE.equals(isSave))
            throw new BusinessException(ErrorCode.SDK_LICENSE_IN_TRY_VERIFY);

        redisTemplate.opsForValue().set(sessionKey, value);
        redisTemplate.opsForValue().set(licenseKey, sessionId);
        redisTemplate.opsForValue().set(triggerKey, "", ttl);
        redisTemplate.opsForValue().set(sequenceKey, "0", ttl);
        redisTemplate.delete(lockKey);
    }

    public void update(String sessionId, SessionValue sessionValue, Duration ttl) {
        String value = toJson(sessionValue);
        String sessionKey = getSessionKeyFormat(sessionId);
        String triggerKey = getTriggerKeyFormat(sessionId);
        redisTemplate.opsForValue().set(sessionKey, value);
        redisTemplate.expire(triggerKey, ttl);
    }

    @Override
    public Long increaseSequence(String sessionId) {
        String sequenceKey = getSequenceKeyFormat(sessionId);
        return redisTemplate.opsForValue().increment(sequenceKey);
    }

    @Override
    public Long findSequenceById(String sessionId) {
        String sequenceKey = getSequenceKeyFormat(sessionId);
        String value = redisTemplate.opsForValue().get(sequenceKey);

        long seq = -1L;
        try {
            seq = Long.parseLong(value);
        } catch (Exception ignore) {
        }
        return seq;
    }

    public Optional<SessionValue> findById(String sessionId) {
        return Optional.ofNullable(getSessionValue(sessionId));
    }

    @Override
    public Optional<SessionValue> findSessionByLicenseId(Long licenseId) {
        String sessionId = getLicenseValue(licenseId);
        SessionValue sessionValue = getSessionValue(sessionId);
        return Optional.ofNullable(sessionValue);
    }

    @Override
    public List<SessionValue> findSessionsByLicenseIds(List<Long> licenseIds) {

        List<String> licenseKeyFormats = new ArrayList<>();
        List<String> sessionKeyFormats = new ArrayList<>();
        licenseIds.forEach(licenseId -> licenseKeyFormats.add(getLicenseKeyFormat(licenseId)));

        List<String> sessionIds = redisTemplate.opsForValue().multiGet(licenseKeyFormats);
        sessionIds.forEach(sessionId -> sessionKeyFormats.add(getSessionKeyFormat(sessionId)));

        List<SessionValue> sessionValues = new ArrayList<>();
        redisTemplate.opsForValue().multiGet(sessionKeyFormats)
                .forEach(json -> {
                    if (!StringUtil.isNullOrEmpty(json))
                        sessionValues.add(fromJson(json));
                });
        return sessionValues;
    }

    public boolean extendTTL(String sessionId, Duration ttl) {
        String triggerKey = getTriggerKeyFormat(sessionId);
        String sequenceKey = getSequenceKeyFormat(sessionId);
        Boolean expire = redisTemplate.expire(triggerKey, ttl) && redisTemplate.expire(sequenceKey, ttl);
        return Boolean.TRUE.equals(expire);
    }

    public void delete(String sessionId) {
        SessionValue sessionValue = findById(sessionId).orElse(null);
        String sessionKey = getSessionKeyFormat(sessionId);
        String triggerKey = getTriggerKeyFormat(sessionId);
        String sequenceKey = getSequenceKeyFormat(sessionId);

        List<String> keys = new ArrayList<>(List.of(sessionKey, triggerKey, sequenceKey));
        if (sessionValue != null)
            keys.add(getLicenseKeyFormat(sessionValue.getLicenseId()));
        redisTemplate.delete(keys);
    }

    @Override
    public void deleteByLicenseId(Long licenseId) {
        String sessionId = getLicenseValue(licenseId);

        List<String> keys = new ArrayList<>();
        keys.add(getLicenseKeyFormat(licenseId));
        if (!StringUtil.isNullOrEmpty(sessionId)) {
            keys.add(getSessionKeyFormat(sessionId));
            keys.add(getTriggerKeyFormat(sessionId));
            keys.add(getSequenceKeyFormat(sessionId));
        }
        redisTemplate.delete(keys);
    }

    private SessionValue getSessionValue(String sessionId) {
        String sessionKey = getSessionKeyFormat(sessionId);
        String json = redisTemplate.opsForValue().get(sessionKey);
        if (StringUtil.isNullOrEmpty(json))
            return null;

        return fromJson(json);
    }

    private String getLicenseValue(Long licenseId) {
        String licenseKey = getLicenseKeyFormat(licenseId);
        return redisTemplate.opsForValue().get(licenseKey);
    }

    private String getSessionKeyFormat(String sessionId) {
        return String.format("%s:%s", SESSION_KEY_PREFIX, sessionId);
    }

    private String getLicenseKeyFormat(Long licenseId) {
        return String.format("%s:%s", SESSION_LICENSE_PREFIX, licenseId);
    }

    private String getTriggerKeyFormat(String sessionId) {
        return String.format("%s:%s", SESSION_TRIGGER_PREFIX, sessionId);
    }

    private String getSequenceKeyFormat(String sessionId) {
        return String.format("%s:%s", SESSION_SEQ_PREFIX, sessionId);
    }

    private String getLockKeyFormat(Long licenseId) {
        return String.format("%s:%s", SESSION_LOCK_PREFIX, licenseId);
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
