package koza.licensemanagementservice.member.service;

import koza.licensemanagementservice.auth.dto.JwtTokenDTO;
import koza.licensemanagementservice.auth.dto.MemberLoginRequest;
import koza.licensemanagementservice.auth.jwt.JwtTokenProvider;
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
        given(jwtTokenProvider.createToken(member).getAccessToken()).willReturn(createAccessToken);
        given(jwtTokenProvider.createToken(member).getRefreshToken()).willReturn(createRefreshToken);

        // when
        JwtTokenDTO token = memberService.login(request, "127.0.0.1", "test-agent");

        // then
        assertThat(token.getAccessToken()).isEqualTo(createAccessToken);
        assertThat(token.getRefreshToken()).isEqualTo(createRefreshToken);

        verify(memberRepository).findByEmail(request.getEmail());
        verify(passwordEncoder).matches(request.getPassword(), encodePassword);
        verify(jwtTokenProvider).createToken(member);
    }
}