package koza.licensemanagementservice.domain.member.service;

import koza.licensemanagementservice.auth.dto.JwtTokenDTO;
import koza.licensemanagementservice.auth.dto.MemberLoginRequest;
import koza.licensemanagementservice.domain.member.dto.request.MemberWithdrawRequest;
import koza.licensemanagementservice.domain.member.entity.JoinType;
import koza.licensemanagementservice.domain.member.entity.MemberStatus;
import koza.licensemanagementservice.domain.member.log.dto.MemberJoinEvent;
import koza.licensemanagementservice.domain.member.log.dto.MemberLoginFailEvent;
import koza.licensemanagementservice.domain.member.log.dto.MemberLoginSuccessEvent;
import koza.licensemanagementservice.domain.member.log.dto.MemberWithdrawEvent;
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberService {
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
        roles.add("ROLE_USER");

        Member member = Member.builder().email(joinRequest.getEmail())
                .nickname(joinRequest.getNickname())
                .password(passwordEncoder.encode(joinRequest.getPassword()))
                .provider("LOCAL")
                .roles(roles)
                .build();

        Member save = memberRepository.save(member);
        publisher.publishEvent(new MemberJoinEvent(save.getId(), save.toSnapshot()));
        return save.getId();
    }

    @Transactional
    public JwtTokenDTO login(MemberLoginRequest memberLoginRequest, String ip, String userAgent) {
        Member member = memberRepository.findByEmail(memberLoginRequest.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.INCORRECT_EMAIL_OR_PASSWORD));

        if (!passwordEncoder.matches(memberLoginRequest.getPassword(), member.getPassword())) {
            publisher.publishEvent(MemberLoginFailEvent.builder()
                    .memberId(member.getId())
                    .joinType(JoinType.LOCAL)
                    .ipAddress(ip)
                    .userAgent(userAgent)
                    .failReason("INCORRECT_PASSWORD")
                    .build());
            throw new BusinessException(ErrorCode.INCORRECT_EMAIL_OR_PASSWORD);
        }

        if (member.getStatus() == MemberStatus.BANNED) {
            publisher.publishEvent(MemberLoginFailEvent.builder()
                    .memberId(member.getId())
                    .joinType(JoinType.LOCAL)
                    .ipAddress(ip)
                    .userAgent(userAgent)
                    .failReason("ACCOUNT_BANNED")
                    .build());
            throw new BusinessException(ErrorCode.MEMBER_BANNED);
        }

        publisher.publishEvent(MemberLoginSuccessEvent.builder()
                .memberId(member.getId())
                .joinType(JoinType.LOCAL)
                .ipAddress(ip)
                .userAgent(userAgent)
                .build());

        return jwtTokenProvider.createToken(member);
    }


    @Transactional
    public void withdraw(CustomUser user, MemberWithdrawRequest request) {
        Member member = memberRepository.findById(user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED));

        if (member.getStatus() == MemberStatus.WITHDRAW)
            throw new BusinessException(ErrorCode.INVALID_REQUEST);

        member.withdraw();
        publisher.publishEvent(new MemberWithdrawEvent(
                user.getId(),
                member.getProvider(),
                member.getGrade(),
                request.getReason(),
                member.getCreateAt()
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
