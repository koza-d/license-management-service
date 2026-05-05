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

    /**
     * 라이센스 상태 유효기간 관리 스케줄러
     * - 밴 기간 끝나면 활성대기상태로 변경
     */
    @Scheduled(cron = "0 * * * * *")
    public void scheduleStatusUpdate() {
        log.info("[라이센스 스케줄러] 상태 만료시간이 지난 상태를 업데이트 합니다.");
        licenseAdminService.processStatusUpdate();
    }

}
