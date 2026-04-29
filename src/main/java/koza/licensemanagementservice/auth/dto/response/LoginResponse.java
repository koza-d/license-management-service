package koza.licensemanagementservice.auth.dto.response;

import koza.licensemanagementservice.auth.dto.JwtTokenDTO;
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
