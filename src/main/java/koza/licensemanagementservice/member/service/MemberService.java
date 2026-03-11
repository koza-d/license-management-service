package koza.licensemanagementservice.member.service;

import koza.licensemanagementservice.auth.dto.MemberLoginRequest;
import koza.licensemanagementservice.member.dto.MemberInfoResponse;
import koza.licensemanagementservice.member.dto.MemberJoinRequest;
import koza.licensemanagementservice.member.entity.Member;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import koza.licensemanagementservice.auth.dto.CustomUser;
import koza.licensemanagementservice.member.repository.MemberRepository;
import koza.licensemanagementservice.auth.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
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
                .roles(roles)
                .build();

        Member save = memberRepository.save(member);
        return save.getId();
    }

    @Transactional(readOnly = true)
    public String login(MemberLoginRequest memberLoginRequest) {
        Member member = memberRepository.findByEmail(memberLoginRequest.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.INCORRECT_EMAIL_OR_PASSWORD));

        if (!passwordEncoder.matches(memberLoginRequest.getPassword(), member.getPassword()))
            throw new BusinessException(ErrorCode.INCORRECT_EMAIL_OR_PASSWORD);

        return jwtTokenProvider.createToken(member);
    }


    @Transactional(readOnly = true)
    public MemberInfoResponse userInfo(CustomUser user) {
        return MemberInfoResponse.builder()
                .email(user.getUsername())
                .nickname(user.getNickname())
                .roles(user.getAuthorities().stream()
                        .map(SimpleGrantedAuthority::getAuthority)
                        .collect(Collectors.toList())
                )
                .build();

    }
}
