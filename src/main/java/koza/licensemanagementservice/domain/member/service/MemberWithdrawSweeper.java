package koza.licensemanagementservice.domain.member.service;

import koza.licensemanagementservice.domain.member.entity.Member;
import koza.licensemanagementservice.domain.member.entity.MemberStatus;
import koza.licensemanagementservice.domain.member.log.dto.MemberWithdrawEvent;
import koza.licensemanagementservice.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberWithdrawSweeper {
    private final MemberRepository memberRepository;
    private final ApplicationEventPublisher publisher;

    @Transactional
    public int sweep() {
        List<Member> expired = memberRepository.findAllByStatusAndWithdrawScheduledAtBefore(
                MemberStatus.PENDING_WITHDRAW, LocalDateTime.now());

        for (Member member : expired) {
            Long memberId = member.getId();
            String provider = member.getProvider();
            var grade = member.getGrade();
            var registerAt = member.getCreateAt();

            member.withdraw();
            publisher.publishEvent(new MemberWithdrawEvent(
                    memberId,
                    provider,
                    grade,
                    "유예기간 만료 자동 익명화",
                    registerAt
            ));
        }
        return expired.size();
    }
}
