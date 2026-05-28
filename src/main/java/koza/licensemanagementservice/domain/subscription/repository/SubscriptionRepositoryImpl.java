package koza.licensemanagementservice.domain.subscription.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import koza.licensemanagementservice.domain.plan.entity.PlanCode;
import koza.licensemanagementservice.domain.subscription.dto.AdminSubscriptionSummaryResponse;
import koza.licensemanagementservice.domain.subscription.dto.QAdminSubscriptionSummaryResponse;
import koza.licensemanagementservice.domain.subscription.dto.condition.SubscriptionAdminSearchCondition;
import koza.licensemanagementservice.domain.subscription.dto.condition.SubscriptionAdminSearchTarget;
import koza.licensemanagementservice.domain.subscription.entity.BillingCycle;
import koza.licensemanagementservice.domain.subscription.entity.SubscriptionStatus;
import koza.licensemanagementservice.global.querydsl.QuerydslOrderUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static koza.licensemanagementservice.domain.member.entity.QMember.member;
import static koza.licensemanagementservice.domain.plan.entity.QPlan.plan;
import static koza.licensemanagementservice.domain.subscription.entity.QSubscription.subscription;

@RequiredArgsConstructor
public class SubscriptionRepositoryImpl implements SubscriptionRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<AdminSubscriptionSummaryResponse> searchSubscriptions(SubscriptionAdminSearchCondition condition, Pageable pageable) {
        List<AdminSubscriptionSummaryResponse> content = queryFactory
                .select(
                        new QAdminSubscriptionSummaryResponse(
                                subscription.id,
                                member.email,
                                member.nickname,
                                plan.planCode,
                                subscription.billingCycle,
                                subscription.status,
                                subscription.currentPeriodStart,
                                subscription.currentPeriodEnd,
                                subscription.startedAt
                        )
                )
                .from(subscription)
                .innerJoin(subscription.member, member)
                .innerJoin(subscription.plan, plan)
                .where(
                        searchKeyword(condition.target(), condition.search()),
                        statusFilter(condition.statusFilters()),
                        planFilter(condition.planCode()),
                        billingCycleFilter(condition.billingCycle()),
                        startedAtBetween(condition.from(), condition.to())
                )
                .orderBy(QuerydslOrderUtil.getOrderSpecifiers(pageable.getSort(), subscription, "id", Set.of("id", "startedAt", "currentPeriodEnd", "cancelledAt")))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(subscription.count())
                .from(subscription)
                .innerJoin(subscription.member, member)
                .innerJoin(subscription.plan, plan)
                .where(
                        searchKeyword(condition.target(), condition.search()),
                        statusFilter(condition.statusFilters()),
                        planFilter(condition.planCode()),
                        billingCycleFilter(condition.billingCycle()),
                        startedAtBetween(condition.from(), condition.to())
                ).fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }

    private BooleanExpression startedAtBetween(LocalDate from, LocalDate to) {
        if (from == null && to == null) return null;

        if (to == null)
            return subscription.startedAt.goe(from.atStartOfDay());

        if (from == null)
            return subscription.startedAt.loe(to.atTime(LocalTime.MAX));

        return subscription.startedAt.between(from.atStartOfDay(), to.atTime(LocalTime.MAX));
    }

    private BooleanExpression billingCycleFilter(BillingCycle billingCycle) {
        if (billingCycle == null)
            return null;

        return subscription.billingCycle.eq(billingCycle);
    }

    private BooleanExpression planFilter(PlanCode planCode) {
        if (planCode == null)
            return null;

        return plan.planCode.eq(planCode);
    }

    private BooleanExpression statusFilter(List<SubscriptionStatus> statusFilters) {
        if (statusFilters == null || statusFilters.isEmpty())
            return subscription.status.eq(SubscriptionStatus.DELETED).not();

        return subscription.status.in(statusFilters);
    }


    private BooleanExpression searchKeyword(SubscriptionAdminSearchTarget target, String search) {
        if (search == null || search.isEmpty())
            return null;

        if (target != null && target != SubscriptionAdminSearchTarget.ALL) {
            return switch (target) {
                case EMAIL -> member.email.containsIgnoreCase(search);
                case NICKNAME -> member.nickname.containsIgnoreCase(search);
                default -> null;
            };
        }

        return member.email.containsIgnoreCase(search)
                .or(member.nickname.containsIgnoreCase(search));
    }
}
