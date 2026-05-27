package koza.licensemanagementservice.domain.payment.entity;

import jakarta.persistence.*;
import koza.licensemanagementservice.domain.billing.toss.dto.CardIssuer;
import koza.licensemanagementservice.domain.billing.toss.dto.TossPaymentResponse;
import koza.licensemanagementservice.domain.member.entity.Member;
import koza.licensemanagementservice.domain.subscription.entity.Subscription;
import koza.licensemanagementservice.global.common.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "payments")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Payment extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "payment_key", length = 255)
    private String paymentKey;

    @Column(name = "order_id", length = 64, nullable = false)
    private String orderId;

    @Column(name = "order_name", length = 255, nullable = false)
    private String orderName;

    @Column(name = "card_type", length = 20)
    private String cardType;

    @Column(name = "card_owner_type", length = 20)
    private String cardOwnerType;

    @Column(name = "card_issuer_code", length = 20)
    private String cardIssuerCode;

    @Column(name = "card_company", length = 20)
    private String cardCompany;

    @Column(name = "card_number_masked", length = 30)
    private String cardNumberMasked;

    @Column(name = "approve_no", length = 8)
    private String approveNo;

    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "receipt_url")
    private String receiptUrl;

    @Column(name = "amount")
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private PaymentStatus status;

    @Column(name = "failure_code", length = 50)
    private String failureCode;

    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    @Column(name = "raw_response", columnDefinition = "TEXT")
    private String rawResponse;

    public boolean fail(String failureCode, String failureReason) {
        if (this.status == PaymentStatus.FAILED)
            return false;
        this.failureCode = failureCode;
        this.failureReason = failureReason;
        this.status = PaymentStatus.FAILED;
        return true;
    }

    public boolean success(TossPaymentResponse paymentResponse, String rawResponse) {
        if (this.status == PaymentStatus.SUCCESS)
            return false;
        this.paymentKey = paymentResponse.getPaymentKey();
        this.approveNo = paymentResponse.getCard().getApproveNo();
        this.requestedAt = paymentResponse.getRequestedAt().atZoneSameInstant(ZoneId.of("Asia/Seoul")).toLocalDateTime();
        this.approvedAt = paymentResponse.getApprovedAt().atZoneSameInstant(ZoneId.of("Asia/Seoul")).toLocalDateTime();
        this.receiptUrl = paymentResponse.getReceipt().getUrl();
        this.rawResponse = rawResponse;
        this.status = PaymentStatus.SUCCESS;
        return true;
    }
}
