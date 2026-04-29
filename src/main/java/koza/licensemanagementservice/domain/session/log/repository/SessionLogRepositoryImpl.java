package koza.licensemanagementservice.domain.session.log.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import koza.licensemanagementservice.domain.session.log.dto.response.QSessionHistoryResponse;
import koza.licensemanagementservice.domain.session.log.dto.condition.SessionLogSearchCondition;
import koza.licensemanagementservice.domain.session.log.dto.response.SessionHistoryResponse;
import koza.licensemanagementservice.stat.dto.response.QSoftwareUsageResponse;
import koza.licensemanagementservice.stat.dto.response.SoftwareUsageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static koza.licensemanagementservice.domain.license.entity.QLicense.license;
import static koza.licensemanagementservice.domain.member.entity.QMember.member;
import static koza.licensemanagementservice.domain.session.log.entity.QSessionLog.sessionLog;
import static koza.licensemanagementservice.domain.software.entity.QSoftware.software;
import static koza.licensemanagementservice.global.querydsl.QuerydslOrderUtil.getOrderSpecifiers;


@RequiredArgsConstructor
public class SessionLogRepositoryImpl implements SessionLogRepositoryCustom {
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

    @Override
    public List<SoftwareUsageResponse> getTopNSoftwareByUsageTime(int n) {
        NumberTemplate<Long> usageMinute = Expressions.numberTemplate(
                Long.class,
                "TIMESTAMPDIFF(MINUTE, {0}, {1})",
                sessionLog.verifyAt,
                sessionLog.releaseAt
        );

        return queryFactory
                .select(
                        new QSoftwareUsageResponse(
                                software.name,
                                member.email,
                                member.nickname,
                                sessionLog.count(),
                                usageMinute.sum()
                        )
                )
                .from(sessionLog)
                .leftJoin(sessionLog.license, license)
                .leftJoin(license.software, software)
                .leftJoin(software.member, member)
                .groupBy(software)
                .orderBy(usageMinute.sum().desc())
                .fetch();
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
