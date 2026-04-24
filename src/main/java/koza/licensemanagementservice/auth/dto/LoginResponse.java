package koza.licensemanagementservice.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class LoginResponse {
    @Setter
    private JwtTokenDTO jwtTokenDTO;
    private boolean withdrawCancelled;
}
