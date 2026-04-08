package koza.licensemanagementservice.auth.dto.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InvalidLoginProvider {
    private String provider;
}
