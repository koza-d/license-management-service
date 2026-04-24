package koza.licensemanagementservice.auth.service;

import koza.licensemanagementservice.auth.dto.*;
import koza.licensemanagementservice.auth.dto.error.InvalidLoginProvider;
import koza.licensemanagementservice.auth.jwt.JwtTokenProvider;
import koza.licensemanagementservice.domain.member.entity.Member;
import koza.licensemanagementservice.domain.member.entity.MemberStatus;
import koza.licensemanagementservice.domain.member.log.dto.MemberJoinEvent;
import koza.licensemanagementservice.domain.member.log.dto.MemberLoginFailEvent;
import koza.licensemanagementservice.domain.member.log.dto.MemberLoginSuccessEvent;
import koza.licensemanagementservice.domain.member.log.dto.MemberWithdrawCancelledEvent;
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
    public LoginResponse login(String providerName, String code, String ip, String userAgent) {
        SocialOAuthClient client = oAuthClientFactory.getClient(providerName);
        String accessToken = client.getAccessToken(code);
        OAuthUserInfo userInfo = client.getUserInfo(accessToken);

        Member member = memberRepository.findByEmail(userInfo.getEmail())
                .orElseGet(() -> {
                    // 가입된 계정 없으면 즉시 가입
                    List<String> roles = new ArrayList<>();
                    roles.add("ROLE_USER");
                    Member saveMember = Member.builder().email(userInfo.getEmail())
                            .nickname(userInfo.getName())
                            .profileURL(userInfo.getPicture())
                            .provider(client.getProvider().getName())
                            .providerId(userInfo.getId())
                            .roles(roles)
                            .build();
                    Member save = memberRepository.save(saveMember);
                    publisher.publishEvent(new MemberJoinEvent(save.getId(), saveMember.toSnapshot()));
                    return save;
                });

        String memberProvider = member.getProvider();
        boolean isOAuthUser = memberProvider != null && memberProvider.equalsIgnoreCase(providerName);

        if (!isOAuthUser) { // 선택한 로그인 소셜 방식으로 가입된 메일이 아닌 경우
            String provider = member.getProvider() == null || member.getProvider().isEmpty() ? "LOCAL" : member.getProvider();

            publishLoginFailedLogEvent(member, ip, userAgent, ErrorCode.OAUTH_NOT_REGISTERED);
            throw new BusinessException(ErrorCode.OAUTH_NOT_REGISTERED, new InvalidLoginProvider(provider));
        }

        if (member.getStatus() == MemberStatus.BANNED) {
            publishLoginFailedLogEvent(member, ip, userAgent, ErrorCode.MEMBER_BANNED);
            throw new BusinessException(ErrorCode.MEMBER_BANNED);
        }

        boolean withdrawCancelled = false;
        if (member.getStatus() == MemberStatus.PENDING_WITHDRAW) {
            member.cancelWithdraw();
            publisher.publishEvent(new MemberWithdrawCancelledEvent(member.getId()));
            withdrawCancelled = true;
        }

        if (member.getProfileURL() == null || member.getProfileURL().isEmpty())
            member.changeProfileURL(userInfo.getPicture());
      
        publishSuccessLogEvent(ip, userAgent, member);

        return new LoginResponse(jwtTokenProvider.createToken(member), withdrawCancelled);
    }

    private void publishSuccessLogEvent(String ip, String userAgent, Member member) {
        publisher.publishEvent(MemberLoginSuccessEvent.builder()
                .memberId(member.getId())
                .provider(member.getProvider())
                .ipAddress(ip)
                .userAgent(userAgent)
                .build());
    }

    private void publishLoginFailedLogEvent(Member member, String ip, String userAgent, ErrorCode errorCode) {
        publisher.publishEvent(MemberLoginFailEvent.builder()
                .memberId(member.getId())
                .provider(member.getProvider())
                .ipAddress(ip)
                .userAgent(userAgent)
                .failReason(errorCode.getCode())
                .build());
    }

    public String getAuthURL(String providerName) {
        return oAuthClientFactory.getClient(providerName).getAuthURL();
    }

}
