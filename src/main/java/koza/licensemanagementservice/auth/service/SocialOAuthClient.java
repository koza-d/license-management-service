package koza.licensemanagementservice.auth.service;

import koza.licensemanagementservice.auth.dto.user.OAuthUserInfo;
import koza.licensemanagementservice.auth.dto.SocialProvider;
import org.springframework.stereotype.Component;

@Component
public interface SocialOAuthClient {
    String getAuthURL();
    String getAccessToken(String code);
    OAuthUserInfo getUserInfo(String accessToken);
    SocialProvider getProvider();
}
