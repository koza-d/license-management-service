package koza.licensemanagementservice.auth.service;

import jakarta.annotation.PostConstruct;
import koza.licensemanagementservice.auth.dto.*;
import koza.licensemanagementservice.auth.dto.user.GoogleUserInfo;
import koza.licensemanagementservice.auth.dto.user.OAuthUserInfo;
import koza.licensemanagementservice.auth.prop.OAuthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class GoogleOAuthClient implements SocialOAuthClient {
    private final OAuthProperties oAuthProperties;
    private final WebClient webClient = WebClient.create();
    private OAuthProperties.Credentials credentials;

    @PostConstruct
    protected void init() {
        this.credentials = oAuthProperties.getGoogle();
    }

    @Override
    public String getAuthURL() {
        return getProvider().getAuthURL()
                + "?client_id=" + credentials.getClientId()
                + "&redirect_uri=" + credentials.getRedirectUri()
                + "&response_type=code"
                + "&scope=" + getProvider().getScope();
    }

    @Override
    public String getAccessToken(String code) {
        return Objects.requireNonNull(webClient.post()
                .uri(getProvider().getTokenURL())
                .header("Accept", "application/json")
                .bodyValue(Map.of(
                        "code", code,
                        "client_id", credentials.getClientId(),
                        "client_secret", credentials.getClientSecret(),
                        "redirect_uri", credentials.getRedirectUri(),
                        "grant_type", "authorization_code"
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    return (String) response.get("access_token");
                })
                .block());
    }

    @Override
    public OAuthUserInfo getUserInfo(String accessToken) {
        SocialProvider provider = getProvider();

        return webClient.get()
                .uri(provider.getUserInfoURL())
                .headers(h -> {
                    h.setBearerAuth(accessToken);
                })
                .retrieve()
                .bodyToMono(GoogleUserInfo.class)
                .block();
    }

    @Override
    public SocialProvider getProvider() {
        return SocialProvider.GOOGLE;
    }
}
