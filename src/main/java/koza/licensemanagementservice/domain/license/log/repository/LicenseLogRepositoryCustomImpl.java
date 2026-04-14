package koza.licensemanagementservice.domain.license.log.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import koza.licensemanagementservice.domain.license.log.dto.LicenseLogResponse;
import koza.licensemanagementservice.domain.license.log.dto.QLicenseLogResponse;
import koza.licensemanagementservice.domain.license.log.entity.LicenseLog;
import koza.licensemanagementservice.domain.license.log.entity.LicenseLogType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static koza.licensemanagementservice.domain.license.log.entity.QLicenseLog.licenseLog;
import static koza.licensemanagementservice.domain.member.entity.QMember.member;

@RequiredArgsConstructor
public class LicenseLogRepositoryCustomImpl implements LicenseLogRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    @Override
    public Page<LicenseLogResponse> findByLicenseId(Long licenseId, LicenseLogSearchCondition condition, Pageable pageable) {
        List<LicenseLogResponse> content = queryFactory
                .select(new QLicenseLogResponse(
                        licenseLog.logType,
                        member.email,
                        member.nickname,
                        licenseLog.data,
                        licenseLog.createAt
                ))
                .from(licenseLog)
                .leftJoin(licenseLog.operator, member)
                .where(
                        typeFilter(condition.getLogType()),
                        createAtBetween(condition.getFrom(), condition.getTo())
                )
                .orderBy(getOrderSpecifier(pageable.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(licenseLog.count())
                .from(licenseLog)
                .leftJoin(licenseLog.operator, member)
                .where(
                        typeFilter(condition.getLogType()),
                        createAtBetween(condition.getFrom(), condition.getTo())
                ).fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    private BooleanExpression typeFilter(LicenseLogType type) {
        return type == null ? null : licenseLog.logType.eq(type);
    }
    private static BooleanExpression createAtBetween(LocalDate from, LocalDate to) {
        if (from == null && to == null) return null;

        if (to == null)
            return licenseLog.createAt.goe(from.atStartOfDay());

        if (from == null)
            return licenseLog.createAt.loe(to.atTime(LocalTime.MAX));

        return licenseLog.createAt.between(from.atStartOfDay(), to.atTime(LocalTime.MAX));
    }

    private OrderSpecifier[] getOrderSpecifier(Sort sort) {
        List<OrderSpecifier> orders = new ArrayList<>();
        PathBuilder<LicenseLog> entityPath = new PathBuilder<>(LicenseLog.class, "licenseLog");

        for (Sort.Order order : sort) {
            Order direction = order.isAscending() ? Order.ASC : Order.DESC;
            String prop = order.getProperty();
            orders.add(new OrderSpecifier(direction, entityPath.get(prop)));
        }

        return orders.toArray(OrderSpecifier[]::new);
    }
}
