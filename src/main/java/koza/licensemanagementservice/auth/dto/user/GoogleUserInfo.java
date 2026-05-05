package koza.licensemanagementservice.auth.dto.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleUserInfo implements OAuthUserInfo {
    private String id;
    private String email;
    private String name;
    private String picture;
}
