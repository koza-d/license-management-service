package koza.licensemanagementservice.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class JwtTokenDTO {
    private String accessToken;
    private String refreshToken;
    private boolean withdrawCancelled;

    public JwtTokenDTO(String accessToken, String refreshToken) {
        this(accessToken, refreshToken, false);
    }
}
