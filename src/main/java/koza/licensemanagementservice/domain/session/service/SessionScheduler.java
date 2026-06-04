package koza.licensemanagementservice.domain.session.service;

import koza.licensemanagementservice.domain.license.repository.LicenseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SessionScheduler {
    private final LicenseRepository licenseRepository;
    private final SessionManager sessionManager;

    @Scheduled(cron = "0 0/10 * * * *")
    public void cleanUpGhostSessions() {
        List<Long> licenseIds = licenseRepository.findIdsByHasActiveSessionTrue();
        if (licenseIds.isEmpty())
            return;

        for (Long licenseId : licenseIds) {
            try {
                sessionManager.cleanUpGhostSession(licenseId);
            } catch (Exception e) {
                log.warn("유령 세션 정리 실패. licenseId = {}", licenseId, e);
            }
        }
    }
}
