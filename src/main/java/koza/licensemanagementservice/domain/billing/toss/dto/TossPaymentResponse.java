package koza.licensemanagementservice.domain.billing.toss.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * https://docs.tosspayments.com/reference#payment-%EA%B0%9D%EC%B2%B4 참조
 */
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TossPaymentResponse {
    private String mId;
    private String paymentKey;
    private String orderId;
    private String orderName;
    private String method;
    private Long totalAmount;
    private String status;
    private Card card;
    private OffsetDateTime requestedAt;
    private OffsetDateTime approvedAt;
    private Receipt receipt;

    @Getter
    @NoArgsConstructor
    public static class Card {
        private Long amount;
        private String issuerCode;
        private String number;
        private String approveNo;
        private String cardType;
        private String ownerType;
    }
    @Getter
    @NoArgsConstructor
    public static class Receipt {
        private String url;
    }
}
