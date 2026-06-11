package koza.licensemanagementservice.domain.paymentmethod.dto;

import koza.licensemanagementservice.domain.billing.toss.dto.TossBillingResponse;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Getter
@Builder
@ToString
public class RegisterPaymentMethodDTO {
    private String billingKey;
    private String cardType;
    private String cardOwnerType;
    private String cardIssuerCode;
    private String cardNumberMasked;
    private LocalDateTime authenticatedAt;

    public static RegisterPaymentMethodDTO of(TossBillingResponse toss) {
        return RegisterPaymentMethodDTO.builder()
                .billingKey(toss.getBillingKey())
                .cardType(toss.getCard().getCardType())
                .cardOwnerType(toss.getCard().getOwnerType())
                .cardIssuerCode(toss.getCard().getIssuerCode())
                .cardNumberMasked(toss.getCard().getNumber())
                .authenticatedAt(toss.getAuthenticatedAt().atZoneSameInstant(ZoneId.of("Asia/Seoul")).toLocalDateTime())
                .build();
    }
}
