package koza.licensemanagementservice.domain.session.repository;

import koza.licensemanagementservice.domain.session.dto.SessionValue;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
public interface SessionRepository {
    void save(String sessionId, SessionValue sessionValue, Duration ttl);
    void update(String sessionId, SessionValue sessionValue, Duration ttl);
    Optional<SessionValue> findById(String sessionId);
    Optional<SessionValue> findSessionByLicenseId(Long licenseId);
    boolean extendTTL(String sessionId, Duration ttl);
    void delete(String sessionId);
    void deleteByLicenseId(Long licenseId);
}
