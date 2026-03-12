package koza.licensemanagementservice.domain.sessionLog.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import koza.licensemanagementservice.domain.sessionLog.entity.SessionLog;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static koza.licensemanagementservice.domain.license.entity.QLicense.license;
import static koza.licensemanagementservice.domain.sessionLog.entity.QSessionLog.sessionLog;


@RequiredArgsConstructor
public class SessionLogRepositoryCustomImpl implements SessionLogRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<SessionLog> findByLicenseId(Long licenseId, Pageable pageable) {
        return findByLicenseId(licenseId, null, null, pageable);
    }

    @Override
    public Page<SessionLog> findByLicenseId(Long licenseId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(sessionLog.license.id.eq(licenseId));
        if (startDate != null)
            builder.and(sessionLog.verifyAt.goe(startDate.atStartOfDay()));

        if (endDate != null)
            builder.and(sessionLog.verifyAt.lt(endDate.atStartOfDay()));

        List<SessionLog> content = queryFactory
                .selectFrom(sessionLog)
                .where(sessionLog.license.id.eq(licenseId))
                .leftJoin(sessionLog.license, license)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(sessionLog.count())
                .from(sessionLog)
                .where(builder)
                .fetchOne();
        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    private OrderSpecifier[] getOrderSpecifier(Sort sort) {
        List<OrderSpecifier> orders = new ArrayList<>();
        PathBuilder<SessionLog> entityPath = new PathBuilder<>(SessionLog.class, "sessionLog");

        for (Sort.Order order : sort) {
            Order direction = order.isAscending() ? Order.ASC : Order.DESC;
            String prop = order.getProperty();
            orders.add(new OrderSpecifier(direction, entityPath.get(prop)));
        }

        return orders.toArray(OrderSpecifier[]::new);
    }
}
