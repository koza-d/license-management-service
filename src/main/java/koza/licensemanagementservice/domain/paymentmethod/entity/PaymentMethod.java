package koza.licensemanagementservice.domain.paymentmethod.entity;

import jakarta.persistence.*;
import koza.licensemanagementservice.domain.member.entity.Member;
import koza.licensemanagementservice.global.common.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_method")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PaymentMethod extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "billing_key", length = 500, nullable = false)
    private String billingKey;

    @Column(name = "card_type", length = 20, nullable = false)
    private String cardType;

    @Column(name = "card_owner_type", length = 20, nullable = false)
    private String cardOwnerType;

    @Column(name = "card_company", length = 20, nullable = false)
    private String cardCompany;

    @Column(name = "card_number_masked", length = 30, nullable = false)
    private String cardNumberMasked;

    @Column(name = "authenticated_at")
    private LocalDateTime authenticatedAt;

    @Column(name = "active")
    private boolean active;

}
