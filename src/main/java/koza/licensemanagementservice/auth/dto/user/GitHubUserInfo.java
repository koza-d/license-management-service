package koza.licensemanagementservice.auth.dto.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubUserInfo implements OAuthUserInfo {
    private String id;
    private String email;

    @JsonProperty("avatar_url")
    private String avatarURL;
    private String name;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPicture() {
        return avatarURL;
    }
}
