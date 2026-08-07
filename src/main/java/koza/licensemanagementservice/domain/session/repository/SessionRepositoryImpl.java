package koza.licensemanagementservice.domain.session.repository;

import ch.qos.logback.core.util.StringUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import koza.licensemanagementservice.domain.session.dto.SessionValue;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
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

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final RedisScript<Long> SAVE_SCRIPT = RedisScript.of(
            new ClassPathResource("scripts/session_save.lua"), Long.class);

    private static final RedisScript<Void> DELETE_SCRIPT = RedisScript.of(
            new ClassPathResource("scripts/session_delete.lua"));

    public void save(String sessionId, SessionValue sessionValue, Duration ttl) {
        String value = toJson(sessionValue);
        String sessionKey = getSessionKeyFormat(sessionId);
        String licenseKey = getLicenseKeyFormat(sessionValue.getLicenseId());
        String triggerKey = getTriggerKeyFormat(sessionId);
        String sequenceKey = getSequenceKeyFormat(sessionId);

        // 성공 시 1, 실패시 0 반환
        Long result = redisTemplate.execute(SAVE_SCRIPT,
                List.of(
                        sessionKey,
                        licenseKey, // SETNX 명령어로 저장
                        triggerKey,
                        sequenceKey
                ),
                value, sessionId, String.valueOf(ttl.toMillis())
        );

        // license:{licenseId} 키가 이미 있을 때 반환
        // 현재는 verify 요청 시 DB 단에서 같은 라이센스의 요청은 낙관적 락으로 직렬로 실행 되기때문에
        // 일어날 가능성 없음
        if (result == 0)
            throw new BusinessException(ErrorCode.SDK_LICENSE_IN_TRY_VERIFY);
    }

    public void update(String sessionId, SessionValue sessionValue, Duration ttl) {
        String value = toJson(sessionValue);
        String sessionKey = getSessionKeyFormat(sessionId);
        String triggerKey = getTriggerKeyFormat(sessionId);
        Boolean isUpdate = redisTemplate.opsForValue().setIfPresent(sessionKey, value);
        if (!isUpdate) // 없는 세션을 덮어씌워서 실제 만료시킨 세션이 부활하는 것 방지
            throw new BusinessException(ErrorCode.SDK_SESSION_EXPIRED);

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

    public void delete(String sessionId, Long licenseId) {
        String sessionKey = getSessionKeyFormat(sessionId);
        String licenseKey = getLicenseKeyFormat(licenseId);
        String triggerKey = getTriggerKeyFormat(sessionId);
        String sequenceKey = getSequenceKeyFormat(sessionId);

        redisTemplate.execute(DELETE_SCRIPT,
                List.of(
                        sessionKey,
                        licenseKey,
                        triggerKey,
                        sequenceKey
                ),
                sessionId);
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
