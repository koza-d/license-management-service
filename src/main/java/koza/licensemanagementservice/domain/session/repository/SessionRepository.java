package koza.licensemanagementservice.domain.session.repository;

import koza.licensemanagementservice.domain.session.dto.SessionValue;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Component
public interface SessionRepository {
    void save(String sessionId, SessionValue sessionValue, Duration ttl);
    void update(String sessionId, SessionValue sessionValue, Duration ttl);
    Long increaseSequence(String sessionId);
    Long findSequenceById(String sessionId);
    Optional<SessionValue> findById(String sessionId);
    Optional<SessionValue> findSessionByLicenseId(Long licenseId);
    List<SessionValue> findSessionsByLicenseIds(List<Long> ids);
    void delete(String sessionId, Long licenseId);
    void deleteByLicenseId(Long licenseId);
}
