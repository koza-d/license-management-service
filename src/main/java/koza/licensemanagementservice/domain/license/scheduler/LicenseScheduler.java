package koza.licensemanagementservice.domain.license.scheduler;

import koza.licensemanagementservice.domain.license.log.service.LicenseLogAdminService;
import koza.licensemanagementservice.domain.license.service.LicenseAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class LicenseScheduler {
    private final LicenseAdminService licenseAdminService;

    @Scheduled(cron = "0 0/5 * * * *")
    public void scheduleLicenseExpiration() {
        log.info("[라이센스 스케줄러] 만료된 라이센스를 로깅합니다.");
        licenseAdminService.updateExpiredLicenseStatus();
    }

}
