package koza.licensemanagementservice.domain.license.log.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import koza.licensemanagementservice.domain.license.entity.License;
import koza.licensemanagementservice.domain.license.log.dto.LicenseExtendLogResponse;
import koza.licensemanagementservice.domain.license.log.dto.QLicenseExtendLogResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static koza.licensemanagementservice.domain.license.log.entity.QLicenseExtendLog.licenseExtendLog;
import static koza.licensemanagementservice.domain.member.entity.QMember.member;

@RequiredArgsConstructor
public class LicenseExtendLogRepositoryCustomImpl implements LicenseExtendLogRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<LicenseExtendLogResponse> findByLicenseId(Long licenseId, LocalDate from, LocalDate to, Pageable pageable) {
        List<LicenseExtendLogResponse> content = queryFactory
                .select(
                        new QLicenseExtendLogResponse(
                                member.email,
                                member.nickname,
                                licenseExtendLog.beforeExpiredAt,
                                licenseExtendLog.afterExpiredAt,
                                licenseExtendLog.periodMs,
                                licenseExtendLog.createAt
                        )
                )
                .from(licenseExtendLog)
                .leftJoin(licenseExtendLog.operator, member)
                .where(
                        createAtBetween(from, to)
                )
                .orderBy(getOrderSpecifier(pageable.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(licenseExtendLog.count())
                .from(licenseExtendLog)
                .leftJoin(licenseExtendLog.operator, member)
                .where(
                        createAtBetween(from, to)
                ).fetchOne();
        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    private static BooleanExpression createAtBetween(LocalDate from, LocalDate to) {
        if (from == null && to == null) return null;

        if (to == null)
            return licenseExtendLog.createAt.goe(from.atStartOfDay());

        if (from == null)
            return licenseExtendLog.createAt.loe(to.atTime(LocalTime.MAX));

        return licenseExtendLog.createAt.between(from.atStartOfDay(), to.atTime(LocalTime.MAX));
    }

    private OrderSpecifier[] getOrderSpecifier(Sort sort) {
        List<OrderSpecifier> orders = new ArrayList<>();
        PathBuilder<License> entityPath = new PathBuilder<>(License.class, "license");

        for (Sort.Order order : sort) {
            Order direction = order.isAscending() ? Order.ASC : Order.DESC;
            String prop = order.getProperty();
            orders.add(new OrderSpecifier(direction, entityPath.get(prop)));
        }

        return orders.toArray(OrderSpecifier[]::new);
    }

}
