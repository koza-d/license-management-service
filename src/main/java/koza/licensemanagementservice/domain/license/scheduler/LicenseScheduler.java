package koza.licensemanagementservice.domain.license.scheduler;

import koza.licensemanagementservice.domain.license.log.service.LicenseLogAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LicenseScheduler {
    private final LicenseLogAdminService licenseLogAdminService;

    @Scheduled(cron = "0 0 * * * *")
    public void scheduleLicenseExpiration() {
        log.info("[라이센스 스케줄러] 만료된 라이센스를 로깅합니다.");
        licenseLogAdminService.recordExpiredLicenseLogs(true, null);
    }
}
