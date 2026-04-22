package koza.licensemanagementservice.auth.prop;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Component
@ConfigurationProperties(prefix = "oauth")
public class OAuthProperties {
    private Credentials google = new Credentials();
    private Credentials github = new Credentials();
    private Credentials naver = new Credentials();

    @Getter
    @Setter
    public static class Credentials {
        private String clientId;
        private String clientSecret;
        private String redirectUri;
    }
}
