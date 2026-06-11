package koza.licensemanagementservice.domain.billing.toss.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TossErrorResponse {
    private String code;
    private String message;
}
