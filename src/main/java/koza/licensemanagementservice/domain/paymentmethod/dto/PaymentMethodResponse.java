package koza.licensemanagementservice.domain.paymentmethod.dto;

import koza.licensemanagementservice.domain.paymentmethod.entity.PaymentMethod;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PaymentMethodResponse {
    private String cardType;
    private String cardOwnerType;
    private String cardCompany;
    private String cardNumberMasked;
    private LocalDateTime authenticatedAt;
    private boolean isDefault;
    private boolean isActive;

    public static PaymentMethodResponse of(PaymentMethod entity) {
        return PaymentMethodResponse.builder()
                .cardType(entity.getCardType())
                .cardOwnerType(entity.getCardOwnerType())
                .cardCompany(entity.getCardCompany())
                .cardNumberMasked(entity.getCardNumberMasked())
                .authenticatedAt(entity.getAuthenticatedAt())
                .isDefault(entity.isDefault())
                .isActive(entity.isActive())
                .build();
    }
}
