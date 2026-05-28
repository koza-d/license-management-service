package koza.licensemanagementservice.domain.subscription.entity;


import jakarta.persistence.*;
import koza.licensemanagementservice.domain.member.entity.Member;
import koza.licensemanagementservice.domain.paymentmethod.entity.PaymentMethod;
import koza.licensemanagementservice.domain.plan.entity.Plan;
import koza.licensemanagementservice.domain.plan.entity.PlanCode;
import koza.licensemanagementservice.global.common.BaseEntity;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Subscription extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "plan_id")
    private Plan plan;

    @ManyToOne
    @JoinColumn(name = "payment_method_id")
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private SubscriptionStatus status;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_cycle", length = 20, nullable = false)
    private BillingCycle billingCycle;

    @Column(name = "current_period_start", nullable = false)
    private LocalDateTime currentPeriodStart;

    @Column(name = "current_period_end", nullable = false)
    private LocalDateTime currentPeriodEnd;

    @Column(name = "next_billing_at")
    private LocalDateTime nextBillingAt;

    @Column(name = "grace_period_end")
    private LocalDateTime gracePeriodEnd;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    public void start(PlanCode planCode) {
        LocalDateTime now = LocalDateTime.now();
        this.status = SubscriptionStatus.ACTIVE;
        this.startedAt = now;
        this.currentPeriodStart = now;
        this.currentPeriodEnd = getPeriodEnd(this.billingCycle, now);
        this.nextBillingAt = this.currentPeriodEnd;
        this.member.changeCurrentPlanCode(planCode);
    }

    public void renewal() {
        this.status = SubscriptionStatus.ACTIVE;
        this.currentPeriodStart = currentPeriodEnd;
        this.currentPeriodEnd = getPeriodEnd(this.billingCycle, this.currentPeriodStart);
        this.nextBillingAt = this.currentPeriodEnd.toLocalDate().atStartOfDay();
        this.cancelledAt = null;
        this.gracePeriodEnd = null;
    }

    public void cancel() {
        if (this.status == SubscriptionStatus.PAST_DUE) {
            expire();
            this.cancelledAt = LocalDateTime.now();
        } else {
            this.status = SubscriptionStatus.CANCELLED;
            this.cancelledAt = LocalDateTime.now();
        }
    }

    public void resume() {
        this.status = SubscriptionStatus.ACTIVE;
        this.cancelledAt = null;
    }

    public void changePaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public void expire() {
        this.status = SubscriptionStatus.EXPIRED;
        this.gracePeriodEnd = null;
        this.nextBillingAt = null;
        this.member.changeCurrentPlanCode(PlanCode.FREE);
    }

    public void failedBilling() {
        if (this.status == SubscriptionStatus.EXPIRED)
            return;

        // 결제일 이후인 경우 3일 유예
        LocalDate today = LocalDate.now();
        if (this.currentPeriodEnd.isAfter(today.atStartOfDay())) {
            this.status = SubscriptionStatus.PAST_DUE;
            this.gracePeriodEnd = this.currentPeriodEnd.plusDays(3);

            // 다음날이 유예기간을 넘지 않는 경우
            LocalDateTime tomorrowStartAt = today.plusDays(1).atStartOfDay();
            if (!tomorrowStartAt.isAfter(this.gracePeriodEnd))
                this.nextBillingAt = tomorrowStartAt;
        }
    }

    public void delete() {
        this.status = SubscriptionStatus.DELETED;
    }

    private LocalDateTime getPeriodEnd(BillingCycle billingCycle, LocalDateTime periodStart) {
        switch (billingCycle) {
            case MONTHLY -> {
                return periodStart.plusMonths(1);
            }
            case YEARLY -> {
                return periodStart.plusYears(1);
            }
            default -> throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
    }
}
