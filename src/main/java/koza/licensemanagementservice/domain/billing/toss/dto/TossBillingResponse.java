package koza.licensemanagementservice.domain.billing.toss.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TossBillingResponse {
    private String mId;
    private String customerKey; // PaymentMethod.userToken
    private OffsetDateTime authenticatedAt;
    private String method;
    private String billingKey;
    private String cardCompany;
    private String cardNumber; // 마스킹된 값
    private CardInfo card;

    @Getter @NoArgsConstructor
    public static class CardInfo {
        private String issuerCode;
        private String acquirerCode;
        private String number;
        private String cardType;
        private String ownerType;
    }
}
