package koza.licensemanagementservice.domain.paymentmethod.dto;

import koza.licensemanagementservice.domain.billing.toss.dto.CardIssuer;
import koza.licensemanagementservice.domain.paymentmethod.entity.PaymentMethod;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PaymentMethodResponse {
    private Long paymentMethodId;
    private String cardType;
    private String cardOwnerType;
    private String cardIssuerCode;
    private String cardCompany;
    private String cardNumberMasked;
    private LocalDateTime authenticatedAt;
    private boolean isDefault;
    private boolean isActive;

    public static PaymentMethodResponse of(PaymentMethod entity) {
        return PaymentMethodResponse.builder()
                .paymentMethodId(entity.getId())
                .cardType(entity.getCardType())
                .cardOwnerType(entity.getCardOwnerType())
                .cardIssuerCode(entity.getCardIssuerCode())
                .cardCompany(CardIssuer.getKoreanName(entity.getCardIssuerCode()))
                .cardNumberMasked(entity.getCardNumberMasked())
                .authenticatedAt(entity.getAuthenticatedAt())
                .isDefault(entity.isDefault())
                .isActive(entity.isActive())
                .build();
    }
}
