package koza.licensemanagementservice.auth.service;

import jakarta.annotation.PostConstruct;
import koza.licensemanagementservice.auth.dto.GitHubUserInfo;
import koza.licensemanagementservice.auth.dto.OAuthUserInfo;
import koza.licensemanagementservice.auth.dto.SocialProvider;
import koza.licensemanagementservice.auth.prop.OAuthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class GithubOAuthClient implements SocialOAuthClient {
    private final OAuthProperties oAuthProperties;
    private final WebClient webClient = WebClient.create();
    private OAuthProperties.Credentials credentials;

    @PostConstruct
    protected void init() {
        this.credentials = oAuthProperties.getGithub();
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
        return webClient.get()
                .uri(getProvider().getUserInfoURL())
                .headers(h -> {
                    h.setBearerAuth(accessToken);
                })
                .retrieve()
                .bodyToMono(GitHubUserInfo.class)
                .block();
    }


    @Override
    public SocialProvider getProvider() {
        return SocialProvider.GITHUB;
    }
}
