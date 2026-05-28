package koza.licensemanagementservice.domain.payment.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import koza.licensemanagementservice.domain.payment.dto.condition.PaymentAdminSearchCondition;
import koza.licensemanagementservice.domain.payment.dto.condition.PaymentAdminSearchTarget;
import koza.licensemanagementservice.domain.payment.dto.response.AdminPaymentSummaryResponse;
import koza.licensemanagementservice.domain.payment.dto.response.QAdminPaymentSummaryResponse;
import koza.licensemanagementservice.domain.payment.entity.PaymentStatus;
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
import static koza.licensemanagementservice.domain.payment.entity.QPayment.payment;

@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<AdminPaymentSummaryResponse> searchPayments(PaymentAdminSearchCondition condition, Pageable pageable) {
        List<AdminPaymentSummaryResponse> content = queryFactory
                .select(
                        new QAdminPaymentSummaryResponse(
                                payment.id,
                                member.email,
                                member.nickname,
                                payment.orderId,
                                payment.orderName,
                                payment.amount,
                                payment.status,
                                payment.approvedAt
                        )
                )
                .from(payment)
                .innerJoin(payment.member, member)
                .where(
                        searchKeyword(condition.target(), condition.search()),
                        statusFilter(condition.statusFilters()),
                        approvedAtBetween(condition.from(), condition.to())
                )
                .orderBy(QuerydslOrderUtil.getOrderSpecifiers(pageable.getSort(), payment, "id", Set.of("id", "amount", "approvedAt", "createAt")))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(payment.count())
                .from(payment)
                .innerJoin(payment.member, member)
                .where(
                        searchKeyword(condition.target(), condition.search()),
                        statusFilter(condition.statusFilters()),
                        approvedAtBetween(condition.from(), condition.to())
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0);
    }

    private BooleanExpression approvedAtBetween(LocalDate from, LocalDate to) {
        if (from == null && to == null) return null;
        if (to == null) return payment.approvedAt.goe(from.atStartOfDay());
        if (from == null) return payment.approvedAt.loe(to.atTime(LocalTime.MAX));
        return payment.approvedAt.between(from.atStartOfDay(), to.atTime(LocalTime.MAX));
    }

    private BooleanExpression statusFilter(List<PaymentStatus> statusFilters) {
        if (statusFilters == null || statusFilters.isEmpty()) return null;
        return payment.status.in(statusFilters);
    }

    private BooleanExpression searchKeyword(PaymentAdminSearchTarget target, String search) {
        if (search == null || search.isEmpty()) return null;

        if (target != null && target != PaymentAdminSearchTarget.ALL) {
            return switch (target) {
                case EMAIL -> member.email.containsIgnoreCase(search);
                case NICKNAME -> member.nickname.containsIgnoreCase(search);
                case ORDER_ID -> payment.orderId.containsIgnoreCase(search);
                case PAYMENT_KEY -> payment.paymentKey.containsIgnoreCase(search);
                default -> null;
            };
        }

        return member.email.containsIgnoreCase(search)
                .or(member.nickname.containsIgnoreCase(search))
                .or(payment.orderId.containsIgnoreCase(search))
                .or(payment.paymentKey.containsIgnoreCase(search));
    }
}
