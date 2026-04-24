package koza.licensemanagementservice.member.service;

import koza.licensemanagementservice.domain.member.entity.Member;
import koza.licensemanagementservice.domain.member.entity.MemberStatus;
import koza.licensemanagementservice.domain.member.log.dto.MemberWithdrawEvent;
import koza.licensemanagementservice.domain.member.repository.MemberRepository;
import koza.licensemanagementservice.domain.member.service.MemberWithdrawSweeper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MemberWithdrawSweeperTest {

    @InjectMocks
    private MemberWithdrawSweeper sweeper;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ApplicationEventPublisher publisher;

    @Test
    @DisplayName("유예기간 만료된 PENDING_WITHDRAW 회원을 익명화하고 이벤트 발행")
    void sweep_anonymizes_expired_members() {
        // given
        Member m1 = Member.builder().id(1L).email("a@test.com").nickname("u1").provider("GOOGLE").build();
        Member m2 = Member.builder().id(2L).email("b@test.com").nickname("u2").provider("LOCAL").build();
        m1.requestWithdraw(LocalDateTime.now().minusMinutes(1));
        m2.requestWithdraw(LocalDateTime.now().minusDays(1));

        given(memberRepository.findAllByStatusAndWithdrawScheduledAtBefore(eq(MemberStatus.PENDING_WITHDRAW), any()))
                .willReturn(List.of(m1, m2));

        // when
        int processed = sweeper.sweep();

        // then
        assertThat(processed).isEqualTo(2);
        assertThat(m1.getStatus()).isEqualTo(MemberStatus.WITHDRAW);
        assertThat(m1.getEmail()).isEqualTo("탈퇴한유저_1");
        assertThat(m1.getWithdrawScheduledAt()).isNull();
        assertThat(m2.getStatus()).isEqualTo(MemberStatus.WITHDRAW);
        assertThat(m2.getEmail()).isEqualTo("탈퇴한유저_2");
        verify(publisher, times(2)).publishEvent(any(MemberWithdrawEvent.class));
    }

    @Test
    @DisplayName("만료된 회원이 없으면 0 반환, 이벤트 미발행")
    void sweep_does_nothing_when_no_expired_members() {
        // given
        given(memberRepository.findAllByStatusAndWithdrawScheduledAtBefore(eq(MemberStatus.PENDING_WITHDRAW), any()))
                .willReturn(List.of());

        // when
        int processed = sweeper.sweep();

        // then
        assertThat(processed).isZero();
        verify(publisher, times(0)).publishEvent(any());
    }
}
