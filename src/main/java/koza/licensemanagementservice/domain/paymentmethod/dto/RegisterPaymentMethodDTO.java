package koza.licensemanagementservice.domain.paymentmethod.dto;

import koza.licensemanagementservice.domain.billing.toss.dto.TossBillingResponse;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RegisterPaymentMethodDTO {
    private String billingKey;
    private String cardType;
    private String cardOwnerType;
    private String cardCompany;
    private String cardNumberMasked;
    private LocalDateTime authenticatedAt;

    public static RegisterPaymentMethodDTO of(TossBillingResponse toss) {
        return RegisterPaymentMethodDTO.builder()
                .billingKey(toss.getBillingKey())
                .cardType(toss.getCard().getCardType())
                .cardOwnerType(toss.getCard().getOwnerType())
                .cardCompany(toss.getCardCompany())
                .cardNumberMasked(toss.getCardNumber())
                .authenticatedAt(toss.getAuthenticatedAt().toLocalDateTime())
                .build();
    }
}
