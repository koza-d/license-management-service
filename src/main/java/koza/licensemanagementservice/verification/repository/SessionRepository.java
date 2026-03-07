package koza.licensemanagementservice.verification.repository;

import koza.licensemanagementservice.verification.dto.SessionValue;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
public interface SessionRepository {
    void save(String sessionId, SessionValue sessionValue, Duration ttl);
    Optional<SessionValue> findById(String sessionId);
    String findSessionIdByLicenseId(Long licenseId);
    Optional<LocalDateTime> findLatestActiveAtByIdAndTTL(String sessionId, Duration ttl);
    boolean hasSession(String sessionId);
    boolean extendTTL(String sessionId, Duration ttl);
    void delete(String sessionId);
}
