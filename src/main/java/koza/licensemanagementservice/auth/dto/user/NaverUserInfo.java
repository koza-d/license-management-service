package koza.licensemanagementservice.auth.dto.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class NaverUserInfo implements OAuthUserInfo {
    @JsonProperty("response")
    private NaverAccount naverAccount;

    @Override
    public String getId() {
        return naverAccount.getId();
    }

    @Override
    public String getEmail() {
        return naverAccount.getEmail();
    }

    @Override
    public String getName() {
        return naverAccount.getNickname();
    }

    @Override
    public String getPicture() {
        return naverAccount.getProfileImage();
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NaverAccount {
        private String id;
        private String email;
        private String nickname;
        @JsonProperty("profile_image")
        private String profileImage;
    }
}
