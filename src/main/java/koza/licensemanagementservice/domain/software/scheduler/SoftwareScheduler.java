package koza.licensemanagementservice.domain.software.scheduler;

import koza.licensemanagementservice.domain.software.service.SoftwareAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SoftwareScheduler {
    private final SoftwareAdminService softwareAdminService;


    /**
     * 소프트웨어 상태 유효기간 관리 스케줄러
     * - 밴 기간 끝나면 활성대기상태로 변경
     * - 점검 기간 끝나면 활성상태로 변경
     */
    @Scheduled(cron = "0 0/5 * * * *")
    public void scheduleStatusUpdate() {
        log.info("[소프트웨어 스케줄러] 상태 만료시간이 지난 상태를 업데이트 합니다.");
        softwareAdminService.processStatusUpdate();
    }
}
