package koza.licensemanagementservice.session.service;

import koza.licensemanagementservice.license.entity.License;
import koza.licensemanagementservice.license.repository.LicenseRepository;
import koza.licensemanagementservice.member.dto.CustomUser;
import koza.licensemanagementservice.session.dto.SessionResponse;
import koza.licensemanagementservice.software.repository.SoftwareRepository;
import koza.licensemanagementservice.verification.dto.SessionValue;
import koza.licensemanagementservice.verification.service.SessionManager;
import koza.licensemanagementservice.verification.status.SessionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SessionService {
    private final SoftwareRepository softwareRepository;
    private final LicenseRepository licenseRepository;
    private final SessionManager sessionManager;

    public List<SessionResponse> getAllByMember(CustomUser user) {
        List<SessionResponse> sessions = new ArrayList<>();
        for (License license : licenseRepository.findByMemberId(user.getId())) {
            String sessionId = sessionManager.getSessionIdByLicenseId(license.getId());
            Optional<SessionValue> sessionOptional = sessionManager.getSessionValue(sessionId);
            if (sessionOptional.isEmpty())
                continue;

            SessionValue sessionValue = sessionOptional.get();
            SessionStatus status = sessionManager.getStatus(sessionId);
            LocalDateTime latestActiveAt = sessionManager.getLatestActiveAt(sessionId)
                    .orElseGet(() -> LocalDateTime.MIN);
            sessions.add(
                    SessionResponse.builder()
                            .sessionId(sessionId)
                            .licenseKey(license.getLicenseKey())
                            .ipAddress(sessionValue.getIpAddress())
                            .userAgent(sessionValue.getUserAgent())
                            .verifyAt(sessionValue.getVerifyAt())
                            .expireAt(sessionValue.getExpiredAt())
                            .latestActiveAt(latestActiveAt)
                            .status(status)
                            .build()
            );
        }
        return sessions;
    }
}
