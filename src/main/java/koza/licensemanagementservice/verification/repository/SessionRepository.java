package koza.licensemanagementservice.verification.repository;

import koza.licensemanagementservice.verification.dto.SessionValue;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
public interface SessionRepository {
    void save(String sessionId, SessionValue sessionValue, Duration ttl);
    Optional<SessionValue> findById(String sessionId);
    boolean hasSession(String sessionId);
    boolean extendTTL(String sessionId, Duration ttl);
    void delete(String sessionId);
}
