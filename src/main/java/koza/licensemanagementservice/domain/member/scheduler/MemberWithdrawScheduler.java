package koza.licensemanagementservice.domain.member.scheduler;

import koza.licensemanagementservice.domain.member.service.MemberWithdrawSweeper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MemberWithdrawScheduler {
    private final MemberWithdrawSweeper memberWithdrawSweeper;

    @Scheduled(cron = "0 0 4 * * *")
    public void sweepExpiredWithdraws() {
        log.info("[회원탈퇴 스케줄러] 유예기간 만료 회원 익명화 시작");
        int processed = memberWithdrawSweeper.sweep();
        log.info("[회원탈퇴 스케줄러] 익명화 완료 건수={}", processed);
    }
}
