package koza.licensemanagementservice.domain.subscription.scheduler;

import koza.licensemanagementservice.domain.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionScheduler {
    private final SubscriptionService subscriptionService;

    @Scheduled(cron = "0 0 9 * * *")
    public void scheduleRenewal() {
        log.info("[구독 갱신 스케줄러] 구독 갱신을 진행합니다.");
        Pair<Long, Long> result = subscriptionService.scheduleRenewal();
        log.info("[구동 갱신 스케줄러] 구독 갱신 결과 | 성공 {}건 | 실패 {}건", result.getFirst(), result.getSecond());
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void scheduleExpired() {
        log.info("[구독 만료 스케줄러] 만료 조건에 해당하는 구독들을 만료처리합니다.");
        int targets = subscriptionService.scheduleExpired();
        log.info("[구독 만료 스케줄러] {}건의 구독을 만료처리했습니다.", targets);
    }
}
