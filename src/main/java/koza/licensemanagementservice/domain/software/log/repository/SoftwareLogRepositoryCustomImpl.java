package koza.licensemanagementservice.domain.software.log.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import koza.licensemanagementservice.domain.software.log.dto.QSoftwareLogResponse;
import koza.licensemanagementservice.domain.software.log.dto.SoftwareLogResponse;
import koza.licensemanagementservice.domain.software.log.entity.SoftwareLog;
import koza.licensemanagementservice.domain.software.log.entity.SoftwareLogType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static koza.licensemanagementservice.domain.member.entity.QMember.member;
import static koza.licensemanagementservice.domain.software.log.entity.QSoftwareLog.*;

@RequiredArgsConstructor
public class SoftwareLogRepositoryCustomImpl implements SoftwareLogRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<SoftwareLogResponse> findBySoftwareId(Long softwareId, SoftwareLogSearchCondition condition, Pageable pageable) {
        List<SoftwareLogResponse> content = queryFactory
                .select(
                        new QSoftwareLogResponse(
                                softwareLog.logType,
                                member.email,
                                member.nickname,
                                softwareLog.data,
                                softwareLog.createAt
                        )
                )
                .from(softwareLog)
                .where(
                        softwareLog.software.id.eq(softwareId),
                        typeFilter(condition.getLogType()),
                        createAtBetween(condition.getFrom(), condition.getTo())
                )
                .leftJoin(softwareLog.operator, member)
                .orderBy(getOrderSpecifier(pageable.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(
                        softwareLog.count()
                )
                .from(softwareLog)
                .where(
                        softwareLog.software.id.eq(softwareId),
                        typeFilter(condition.getLogType()),
                        createAtBetween(condition.getFrom(), condition.getTo())
                )
                .leftJoin(softwareLog.operator, member)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }


    private BooleanExpression typeFilter(SoftwareLogType logType) {
        return logType == null ? null : softwareLog.logType.eq(logType);
    }

    private static BooleanExpression createAtBetween(LocalDate from, LocalDate to) {
        if (from == null && to == null) return null;

        if (to == null)
            return softwareLog.createAt.goe(from.atStartOfDay());

        if (from == null)
            return softwareLog.createAt.loe(to.atTime(LocalTime.MAX));

        return softwareLog.createAt.between(from.atStartOfDay(), to.atTime(LocalTime.MAX));
    }

    private OrderSpecifier[] getOrderSpecifier(Sort sort) {
        List<OrderSpecifier> orders = new ArrayList<>();
        PathBuilder<SoftwareLog> entityPath = new PathBuilder<>(SoftwareLog.class, "softwareLog");

        for (Sort.Order order : sort) {
            Order direction = order.isAscending() ? Order.ASC : Order.DESC;
            String prop = order.getProperty();
            orders.add(new OrderSpecifier(direction, entityPath.get(prop)));
        }

        return orders.toArray(OrderSpecifier[]::new);
    }

}
