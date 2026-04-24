package koza.licensemanagementservice.member.service;

import koza.licensemanagementservice.auth.dto.CustomUser;
import koza.licensemanagementservice.auth.dto.JwtTokenDTO;
import koza.licensemanagementservice.auth.dto.MemberLoginRequest;
import koza.licensemanagementservice.auth.jwt.JwtTokenProvider;
import koza.licensemanagementservice.domain.member.dto.request.MemberWithdrawRequest;
import koza.licensemanagementservice.domain.member.entity.MemberStatus;
import koza.licensemanagementservice.domain.member.log.dto.MemberWithdrawCancelledEvent;
import koza.licensemanagementservice.domain.member.log.dto.MemberWithdrawRequestedEvent;
import koza.licensemanagementservice.domain.member.service.MemberService;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import koza.licensemanagementservice.domain.member.dto.MemberJoinRequest;
import koza.licensemanagementservice.domain.member.entity.Member;
import koza.licensemanagementservice.domain.member.repository.MemberRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @InjectMocks
    private MemberService memberService; // 테스트 대상

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private ApplicationEventPublisher publisher;

    @Test
    @DisplayName("회원가입 성공 - 새로운 이메일인 경우")
    void join_success() {
        // given (준비)
        MemberJoinRequest request = new MemberJoinRequest("test@test.com", "닉네임", "password123");

        // 가짜 객체들의 행동 정의 (Stubbing)
        given(memberRepository.findByEmail(anyString())).willReturn(Optional.empty()); // 무조건 중복 아니라고 반환
        given(passwordEncoder.encode(anyString())).willReturn("encoded_password");

        // save 호출 시 ID가 들어있는 Member 객체를 반환하도록 설정
        Member savedMember = Member.builder().id(1L).email(request.getEmail()).build();
        given(memberRepository.save(any(Member.class))).willReturn(savedMember);

        // when (실행)
        Long memberId = memberService.join(request);

        // then (검증)
        assertThat(memberId).isNotNull();
        verify(memberRepository, times(1)).save(any(Member.class)); // save가 실제로 한 번 호출됐는지 확인
    }

    @Test
    @DisplayName("회원가입 실패 - 이미 존재하는 이메일")
    void join_fail_duplicate_email() {
        // given
        MemberJoinRequest request = new MemberJoinRequest("test@test.com", "닉네임", "password123");
        Member existingMember = Member.builder().email(request.getEmail()).build();

        given(memberRepository.findByEmail(request.getEmail())).willReturn(Optional.of(existingMember));

        // when
        BusinessException exception = assertThrows(BusinessException.class, () -> memberService.join(request));

        // then
        assertThat(exception.getError()).isEqualTo(ErrorCode.DUPLICATE_EMAIL);
    }

    @Test
    @DisplayName("로그인 성공 - 올바른 정보 입력")
    void login_success() {
        // given
        MemberLoginRequest request = new MemberLoginRequest("test@test.com", "password123");
        String encodePassword = "encodedPassword";
        String createAccessToken = "access-token";
        String createRefreshToken = "refresh-token";

        Member member = Member.builder()
                .email(request.getEmail())
                .password(encodePassword)
                .build();

        given(memberRepository.findByEmail(request.getEmail())).willReturn(Optional.of(member));
        given(passwordEncoder.matches(request.getPassword(), encodePassword)).willReturn(true);
        given(jwtTokenProvider.createToken(member))
                .willReturn(new JwtTokenDTO(createAccessToken, createRefreshToken));

        // when
        JwtTokenDTO token = memberService.login(request, "127.0.0.1", "test-agent");

        // then
        assertThat(token.getAccessToken()).isEqualTo(createAccessToken);
        assertThat(token.getRefreshToken()).isEqualTo(createRefreshToken);
        assertThat(token.isWithdrawCancelled()).isFalse();

        verify(memberRepository).findByEmail(request.getEmail());
        verify(passwordEncoder).matches(request.getPassword(), encodePassword);
        verify(jwtTokenProvider).createToken(member);
    }

    @Test
    @DisplayName("회원 탈퇴 요청 - 즉시 익명화하지 않고 14일 예약 상태로 전환")
    void withdraw_schedules_grace_period() {
        // given
        Long memberId = 42L;
        Member member = Member.builder().id(memberId).email("u@test.com").build();
        CustomUser user = new CustomUser(member.getId(), member.getEmail(), null, null, java.util.Collections.emptyList());

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

        // when
        memberService.withdraw(user, new MemberWithdrawRequest("이유"));

        // then
        assertThat(member.getStatus()).isEqualTo(MemberStatus.PENDING_WITHDRAW);
        assertThat(member.getWithdrawScheduledAt())
                .isAfter(LocalDateTime.now().plusDays(MemberService.WITHDRAW_GRACE_DAYS - 1))
                .isBefore(LocalDateTime.now().plusDays(MemberService.WITHDRAW_GRACE_DAYS + 1));
        assertThat(member.getEmail()).isEqualTo("u@test.com"); // 아직 익명화되지 않음
        verify(publisher).publishEvent(any(MemberWithdrawRequestedEvent.class));
    }

    @Test
    @DisplayName("회원 탈퇴 - 이미 예약 상태인 경우 재요청 불가")
    void withdraw_fails_when_already_pending() {
        // given
        Long memberId = 7L;
        Member member = Member.builder().id(memberId).email("u@test.com").build();
        member.requestWithdraw(LocalDateTime.now().plusDays(14));
        CustomUser user = new CustomUser(member.getId(), member.getEmail(), null, null, java.util.Collections.emptyList());

        given(memberRepository.findById(memberId)).willReturn(Optional.of(member));

        // when / then
        assertThrows(BusinessException.class,
                () -> memberService.withdraw(user, new MemberWithdrawRequest("재요청")));
    }

    @Test
    @DisplayName("로그인 시 PENDING_WITHDRAW 상태면 자동 복구되고 플래그가 true 로 응답")
    void login_cancels_pending_withdraw() {
        // given
        MemberLoginRequest request = new MemberLoginRequest("test@test.com", "password123");
        Member member = Member.builder()
                .email(request.getEmail())
                .password("encoded")
                .build();
        member.requestWithdraw(LocalDateTime.now().plusDays(14));

        given(memberRepository.findByEmail(request.getEmail())).willReturn(Optional.of(member));
        given(passwordEncoder.matches(request.getPassword(), "encoded")).willReturn(true);
        given(jwtTokenProvider.createToken(member))
                .willReturn(new JwtTokenDTO("access", "refresh"));

        // when
        JwtTokenDTO token = memberService.login(request, "127.0.0.1", "agent");

        // then
        assertThat(token.isWithdrawCancelled()).isTrue();
        assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
        assertThat(member.getWithdrawScheduledAt()).isNull();
        verify(publisher).publishEvent(any(MemberWithdrawCancelledEvent.class));
    }
}