package koza.licensemanagementservice.domain.member.service;

import jakarta.servlet.http.HttpServletRequest;
import koza.licensemanagementservice.auth.dto.JwtTokenDTO;
import koza.licensemanagementservice.auth.dto.MemberLoginRequest;
import koza.licensemanagementservice.auth.dto.SocialProvider;
import koza.licensemanagementservice.domain.member.dto.request.MemberWithdrawRequest;
import koza.licensemanagementservice.domain.member.entity.MemberStatus;
import koza.licensemanagementservice.domain.member.log.dto.MemberJoinEvent;
import koza.licensemanagementservice.domain.member.log.dto.MemberLoginFailEvent;
import koza.licensemanagementservice.domain.member.log.dto.MemberLoginSuccessEvent;
import koza.licensemanagementservice.domain.member.log.dto.MemberWithdrawCancelledEvent;
import koza.licensemanagementservice.domain.member.log.dto.MemberWithdrawRequestedEvent;
import koza.licensemanagementservice.domain.member.repository.MemberRepository;
import koza.licensemanagementservice.domain.member.dto.MemberInfoResponse;
import koza.licensemanagementservice.domain.member.dto.MemberJoinRequest;
import koza.licensemanagementservice.domain.member.entity.Member;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import koza.licensemanagementservice.auth.dto.CustomUser;
import koza.licensemanagementservice.auth.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static koza.licensemanagementservice.global.util.RequestIPAddressParser.*;

@Service
@RequiredArgsConstructor
public class MemberService {
    public static final int WITHDRAW_GRACE_DAYS = 14;

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final ApplicationEventPublisher publisher;

    @Transactional
    public Long join(MemberJoinRequest joinRequest) {
        memberRepository.findByEmail(joinRequest.getEmail())
                .ifPresent(m -> {
                    throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
                });
        List<String> roles = new ArrayList<>();
        roles.add("ROLE_MEMBER");

        Member member = Member.builder().email(joinRequest.getEmail())
                .nickname(joinRequest.getNickname())
                .password(passwordEncoder.encode(joinRequest.getPassword()))
                .provider(SocialProvider.LOCAL.getName())
                .roles(roles)
                .build();

        Member save = memberRepository.save(member);
        publisher.publishEvent(new MemberJoinEvent(save.getId(), save.toSnapshot()));
        return save.getId();
    }

    @Transactional
    public JwtTokenDTO login(MemberLoginRequest memberLoginRequest, HttpServletRequest servletRequest) {
        Member member = memberRepository.findByEmail(memberLoginRequest.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.INCORRECT_EMAIL_OR_PASSWORD));

        String userAgent = servletRequest.getHeader("User-Agent");
        String ipAddress = parseIpAddress(servletRequest);

        if (!passwordEncoder.matches(memberLoginRequest.getPassword(), member.getPassword())) {
            publishLoginFailedLogEvent(member, ipAddress, userAgent, ErrorCode.INCORRECT_EMAIL_OR_PASSWORD);
            throw new BusinessException(ErrorCode.INCORRECT_EMAIL_OR_PASSWORD);
        }

        if (member.getStatus() == MemberStatus.BANNED) {
            publishLoginFailedLogEvent(member, ipAddress, userAgent, ErrorCode.MEMBER_BANNED);
            throw new BusinessException(ErrorCode.MEMBER_BANNED);
        }
        
        boolean withdrawCancelled = false;
        if (member.getStatus() == MemberStatus.PENDING_WITHDRAW) {
            member.cancelWithdraw();
            publisher.publishEvent(new MemberWithdrawCancelledEvent(member.getId()));
            withdrawCancelled = true;
        }

        publishLoginSuccessLogEvent(member, ipAddress, userAgent);
    }

        JwtTokenDTO token = jwtTokenProvider.createToken(member);
        return new JwtTokenDTO(token.getAccessToken(), token.getRefreshToken(), withdrawCancelled);
    }

    private void publishLoginSuccessLogEvent(Member member, String ipAddress, String userAgent) {
        publisher.publishEvent(MemberLoginSuccessEvent.builder()
                .memberId(member.getId())
                .provider(member.getProvider())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build());
    }

    private void publishLoginFailedLogEvent(Member member, String ipAddress, String userAgent, ErrorCode errorCode) {
        publisher.publishEvent(MemberLoginFailEvent.builder()
                .memberId(member.getId())
                .provider(member.getProvider())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .failReason(errorCode.getCode())
                .build());
    }



    @Transactional
    public void withdraw(CustomUser user, MemberWithdrawRequest request) {
        Member member = memberRepository.findById(user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        if (member.getStatus() == MemberStatus.WITHDRAW
                || member.getStatus() == MemberStatus.PENDING_WITHDRAW)
            throw new BusinessException(ErrorCode.INVALID_REQUEST);

        LocalDateTime scheduledAt = LocalDateTime.now().plusDays(WITHDRAW_GRACE_DAYS);
        member.requestWithdraw(scheduledAt);
        publisher.publishEvent(new MemberWithdrawRequestedEvent(
                user.getId(),
                request.getReason(),
                scheduledAt
        ));
    }

    @Transactional(readOnly = true)
    public MemberInfoResponse userInfo(CustomUser user) {
        return MemberInfoResponse.builder()
                .email(user.getUsername())
                .nickname(user.getNickname())
                .profileImageURL(user.getProfileURL())
                .roles(user.getAuthorities() == null
                        ? new ArrayList<>()
                        : user.getAuthorities().stream()
                        .map(SimpleGrantedAuthority::getAuthority)
                        .collect(Collectors.toList())
                )
                .build();
    }
}
