package koza.licensemanagementservice.auth.service;

import koza.licensemanagementservice.auth.dto.*;
import koza.licensemanagementservice.auth.dto.error.InvalidLoginProvider;
import koza.licensemanagementservice.auth.jwt.JwtTokenProvider;
import koza.licensemanagementservice.domain.member.entity.JoinType;
import koza.licensemanagementservice.domain.member.entity.Member;
import koza.licensemanagementservice.domain.member.entity.MemberStatus;
import koza.licensemanagementservice.domain.member.log.dto.MemberLoginFailEvent;
import koza.licensemanagementservice.domain.member.log.dto.MemberLoginSuccessEvent;
import koza.licensemanagementservice.domain.member.repository.MemberRepository;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class OAuthService {
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final SocialOAuthClientFactory oAuthClientFactory;
    private final ApplicationEventPublisher publisher;

    @Transactional
    public JwtTokenDTO login(String providerName, String code, String ip, String userAgent) {
        SocialOAuthClient client = oAuthClientFactory.getClient(providerName);
        String accessToken = client.getAccessToken(code);
        OAuthUserInfo userInfo = client.getUserInfo(accessToken);
        JoinType joinType = JoinType.from(client.getProvider().getName());

        Member member = memberRepository.findByEmail(userInfo.getEmail())
                .orElseGet(() -> {
                    // 가입된 계정 없으면 즉시 가입
                    List<String> roles = new ArrayList<>();
                    roles.add("ROLE_USER");
                    return memberRepository.save(Member.builder().email(userInfo.getEmail())
                            .nickname(userInfo.getName())
                            .profileURL(userInfo.getPicture())
                            .provider(client.getProvider().getName())
                            .providerId(userInfo.getId())
                            .roles(roles)
                            .build());
                });

        boolean isOAuthUser = member.getProviderId() != null
                && member.getProviderId().equals(userInfo.getId())
                && member.getProvider() != null
                && member.getProvider().equalsIgnoreCase(client.getProvider().getName());

        if (!isOAuthUser) { // 간편 회원가입으로 가입된 메일이 아닌 경우
            publisher.publishEvent(MemberLoginFailEvent.builder()
                    .memberId(member.getId())
                    .joinType(joinType)
                    .ipAddress(ip)
                    .userAgent(userAgent)
                    .failReason("PROVIDER_MISMATCH")
                    .build());
            String provider = member.getProvider() == null || member.getProvider().isEmpty() ? "local" : member.getProvider();
            throw new BusinessException(ErrorCode.OAUTH_NOT_REGISTERED, new InvalidLoginProvider(provider));
        }

        if (member.getStatus() == MemberStatus.SUSPENDED) {
            publisher.publishEvent(MemberLoginFailEvent.builder()
                    .memberId(member.getId())
                    .joinType(joinType)
                    .ipAddress(ip)
                    .userAgent(userAgent)
                    .failReason("ACCOUNT_SUSPENDED")
                    .build());
            throw new BusinessException(ErrorCode.MEMBER_SUSPENDED);
        }

        if (member.getProfileURL() == null || member.getProfileURL().isEmpty())
            member.changeProfileURL(userInfo.getPicture());

        publisher.publishEvent(MemberLoginSuccessEvent.builder()
                .memberId(member.getId())
                .joinType(joinType)
                .ipAddress(ip)
                .userAgent(userAgent)
                .build());

        return jwtTokenProvider.createToken(member);
    }

    public String getAuthURL(String providerName) {
        return oAuthClientFactory.getClient(providerName).getAuthURL();
    }

}
