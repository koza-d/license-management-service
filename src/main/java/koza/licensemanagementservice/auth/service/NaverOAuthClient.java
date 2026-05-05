package koza.licensemanagementservice.auth.service;

import jakarta.annotation.PostConstruct;
import koza.licensemanagementservice.auth.dto.user.NaverUserInfo;
import koza.licensemanagementservice.auth.dto.user.OAuthUserInfo;
import koza.licensemanagementservice.auth.dto.SocialProvider;
import koza.licensemanagementservice.auth.prop.OAuthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NaverOAuthClient implements SocialOAuthClient {
    private final OAuthProperties oAuthProperties;
    private final WebClient webClient = WebClient.create();
    private OAuthProperties.Credentials credentials;

    @PostConstruct
    protected void init() {
        this.credentials = oAuthProperties.getNaver();
    }

    @Override
    public String getAuthURL() {
        SocialProvider provider = getProvider();

        return provider.getAuthURL()
                + "?client_id=" + credentials.getClientId()
                + "&redirect_uri=" + credentials.getRedirectUri()
                + "&response_type=code"
                + "&scope=" + provider.getScope()
                + "&state=" + UUID.randomUUID();

        /*
         * state는 임시로 랜덤값 넣어둠. 추후 변경 필요
         *
         * 1. 내 서버가 로그인 URL 생성 시
         *    → state 생성 (abc123)
         *    → Redis에 저장 (key: state:abc123, TTL: 5분)
         *    → URL에 포함해서 반환
         *
         * 2. 네이버 로그인 완료 후 callback
         *    → state=abc123 돌아옴
         *    → Redis에 state:abc123 있는지 확인
         *    → 있으면 정상 → Redis에서 삭제 (1회용)
         *    → 없으면 차단
         */
    }

    @Override
    public String getAccessToken(String code) {
        return Objects.requireNonNull(webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("nid.naver.com")
                        .path("/oauth2.0/token")
                        .queryParam("grant_type", "authorization_code")
                        .queryParam("client_id", credentials.getClientId())
                        .queryParam("client_secret", credentials.getClientSecret())
                        .queryParam("code", code)
                        .queryParam("state", UUID.randomUUID().toString())
                        .build()
                )
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> (String) response.get("access_token"))
                .block());
    }

    @Override
    public OAuthUserInfo getUserInfo(String accessToken) {
        return webClient.get()
                .uri(getProvider().getUserInfoURL())
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(NaverUserInfo.class)
                .block();
    }

    @Override
    public SocialProvider getProvider() {
        return SocialProvider.NAVER;
    }
}
