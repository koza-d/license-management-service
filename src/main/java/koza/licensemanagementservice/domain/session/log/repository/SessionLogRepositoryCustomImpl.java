package koza.licensemanagementservice.domain.session.log.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import koza.licensemanagementservice.domain.session.log.dto.QSessionHistoryResponse;
import koza.licensemanagementservice.domain.session.log.dto.SessionHistoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static koza.licensemanagementservice.domain.session.log.entity.QSessionLog.sessionLog;
import static koza.licensemanagementservice.global.querydsl.QuerydslOrderUtil.getOrderSpecifiers;


@RequiredArgsConstructor
public class SessionLogRepositoryCustomImpl implements SessionLogRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<SessionHistoryResponse> findByLicenseId(Long licenseId, Pageable pageable) {
        return findByLicenseId(licenseId, new SessionLogSearchCondition(), pageable);
    }

    @Override
    public Page<SessionHistoryResponse> findByLicenseId(Long licenseId, SessionLogSearchCondition condition, Pageable pageable) {
        List<SessionHistoryResponse> content = queryFactory
                .select(
                        new QSessionHistoryResponse(
                                sessionLog.sessionId,
                                sessionLog.ipAddress,
                                sessionLog.userAgent,
                                sessionLog.verifyAt,
                                sessionLog.releaseAt,
                                sessionLog.releaseType
                        )
                )
                .from(sessionLog)
                .where(
                        sessionLog.license.id.eq(licenseId),
                        verifyAtBetween(condition.getFrom(), condition.getTo())
                )
                .orderBy(getOrderSpecifiers(pageable.getSort(), sessionLog, "id", Set.of("id", "createAt")))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(sessionLog.count())
                .from(sessionLog)
                .where(
                        sessionLog.license.id.eq(licenseId),
                        verifyAtBetween(condition.getFrom(), condition.getTo())
                )
                .fetchOne();
        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    private static BooleanExpression verifyAtBetween(LocalDate from, LocalDate to) {
        if (from == null && to == null) return null;

        if (to == null)
            return sessionLog.verifyAt.goe(from.atStartOfDay());

        if (from == null)
            return sessionLog.verifyAt.loe(to.atTime(LocalTime.MAX));

        return sessionLog.verifyAt.between(from.atStartOfDay(), to.atTime(LocalTime.MAX));
    }
}
