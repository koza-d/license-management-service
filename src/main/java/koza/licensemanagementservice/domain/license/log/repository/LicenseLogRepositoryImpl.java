package koza.licensemanagementservice.domain.license.log.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import koza.licensemanagementservice.domain.license.entity.LicenseStatus;
import koza.licensemanagementservice.domain.license.log.dto.response.LicenseLogResponse;
import koza.licensemanagementservice.domain.license.log.dto.response.QLicenseLogResponse;
import koza.licensemanagementservice.domain.license.log.dto.condition.LicenseLogSearchCondition;
import koza.licensemanagementservice.domain.license.log.entity.LicenseLogType;
import koza.licensemanagementservice.stat.dto.response.LicenseStatusTrendResponse;
import koza.licensemanagementservice.stat.dto.response.QLicenseStatusTrendResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static koza.licensemanagementservice.domain.license.log.entity.QLicenseLog.licenseLog;
import static koza.licensemanagementservice.domain.member.entity.QMember.member;
import static koza.licensemanagementservice.global.querydsl.QuerydslOrderUtil.getOrderSpecifiers;

@RequiredArgsConstructor
public class LicenseLogRepositoryImpl implements LicenseLogRepositoryCustom {
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
                        licenseLog.license.id.eq(licenseId),
                        typeFilter(condition.getLogType()),
                        createAtBetween(condition.getFrom(), condition.getTo())
                )
                .orderBy(getOrderSpecifiers(pageable.getSort(), licenseLog, "id", Set.of("id", "createAt")))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(licenseLog.count())
                .from(licenseLog)
                .leftJoin(licenseLog.operator, member)
                .where(
                        licenseLog.license.id.eq(licenseId),
                        typeFilter(condition.getLogType()),
                        createAtBetween(condition.getFrom(), condition.getTo())
                ).fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    @Override
    public List<LicenseStatusTrendResponse> getLicenseStatusTrendsByDate(LocalDate from, LocalDate to) {
        StringTemplate formattedDate = Expressions.stringTemplate(
                "DATE_FORMAT({0}, '%Y-%m-%d')",
                licenseLog.operatedAt);

        StringTemplate afterStatus = Expressions.stringTemplate(
                "JSON_UNQUOTE(JSON_EXTRACT({0}, '$.status.after'))",
                licenseLog.data);

        return queryFactory
                .select(
                        new QLicenseStatusTrendResponse(
                                formattedDate,
                                licenseLog.logType.when(LicenseLogType.ISSUED).then(1L).otherwise(Expressions.nullExpression()).count(),
                                licenseLog.logType.when(LicenseLogType.EXPIRED).then(1L).otherwise(Expressions.nullExpression()).count(),
                                licenseLog.logType.when(LicenseLogType.CHANGED_STATUS).then(1L).otherwise(Expressions.nullExpression()).count()
                        )
                )
                .from(licenseLog)
                .where(
                        createAtBetween(from, to),
                        licenseLog.logType.in(LicenseLogType.ISSUED, LicenseLogType.EXPIRED)
                                .or(
                                        licenseLog.logType.eq(LicenseLogType.CHANGED_STATUS)
                                                .and(afterStatus.eq(LicenseStatus.BANNED.name()))
                                )
                )
                .groupBy(formattedDate)
                .orderBy(formattedDate.asc())
                .fetch();
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
}
