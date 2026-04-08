package koza.licensemanagementservice.auth.service;

import koza.licensemanagementservice.auth.dto.*;
import koza.licensemanagementservice.auth.dto.error.InvalidLoginProvider;
import koza.licensemanagementservice.auth.jwt.JwtTokenProvider;
import koza.licensemanagementservice.domain.member.entity.Member;
import koza.licensemanagementservice.domain.member.repository.MemberRepository;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class OAuthService {
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final SocialOAuthClientFactory oAuthClientFactory;

    public JwtTokenDTO login(String providerName, String code) {
        SocialOAuthClient client = oAuthClientFactory.getClient(providerName);
        String accessToken = client.getAccessToken(code);
        OAuthUserInfo userInfo = client.getUserInfo(accessToken);

        Member member = memberRepository.findByEmail(userInfo.getEmail())
                .orElseGet(() -> {
                    // 가입된 계정 없으면 즉시 가입
                    List<String> roles = new ArrayList<>();
                    roles.add("USER");
                    return memberRepository.save(Member.builder().email(userInfo.getEmail())
                            .nickname(userInfo.getName())
                            .provider(client.getProvider().getName())
                            .providerId(userInfo.getId())
                            .roles(roles)
                            .build());
                });

        boolean isOAuthUser = member.getProviderId().equals(userInfo.getId())
                && member.getProvider().equalsIgnoreCase(client.getProvider().getName());

        if (!isOAuthUser) { // 간편 회원가입으로 가입된 메일이 아닌 경우
            String provider = member.getProvider() == null || member.getProvider().isEmpty() ? "local" : member.getProvider();
            throw new BusinessException(ErrorCode.OAUTH_NOT_REGISTERED, new InvalidLoginProvider(provider));
        }

        return jwtTokenProvider.createToken(member);
    }

    public String getAuthURL(String providerName) {
        return oAuthClientFactory.getClient(providerName).getAuthURL();
    }

}
