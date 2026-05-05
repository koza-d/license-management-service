package koza.licensemanagementservice.domain.software.log.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import koza.licensemanagementservice.domain.software.log.dto.response.QSoftwareLogResponse;
import koza.licensemanagementservice.domain.software.log.dto.condition.SoftwareLogSearchCondition;
import koza.licensemanagementservice.domain.software.log.dto.response.SoftwareLogResponse;
import koza.licensemanagementservice.domain.software.log.entity.SoftwareLogType;
import koza.licensemanagementservice.stat.dto.response.QSoftwareRegisterTrendResponse;
import koza.licensemanagementservice.stat.dto.response.SoftwareRegisterTrendResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static koza.licensemanagementservice.domain.member.entity.QMember.member;
import static koza.licensemanagementservice.domain.software.log.entity.QSoftwareLog.*;
import static koza.licensemanagementservice.global.querydsl.QuerydslOrderUtil.getOrderSpecifiers;

@RequiredArgsConstructor
public class SoftwareLogRepositoryImpl implements SoftwareLogRepositoryCustom {
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
                .leftJoin(softwareLog.operator, member)
                .where(
                        softwareLog.software.id.eq(softwareId),
                        typeFilter(condition.getLogType()),
                        createAtBetween(condition.getFrom(), condition.getTo())
                )
                .orderBy(getOrderSpecifiers(pageable.getSort(), softwareLog, "id", Set.of("id", "createAt")))
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
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    @Override
    public List<SoftwareRegisterTrendResponse> getSoftwareRegistrationTrends(LocalDate from, LocalDate to) {
        StringTemplate formattedDate = Expressions.stringTemplate(
                "DATE_FORMAT({0}, '%Y-%m-%d')",
                softwareLog.createAt);

        return queryFactory
                .select(
                        new QSoftwareRegisterTrendResponse(
                                formattedDate,
                                softwareLog.logType.when(SoftwareLogType.REGISTER).then(1L).otherwise(Expressions.nullExpression()).count()
                        )
                )
                .from(softwareLog)
                .where(
                        createAtBetween(from, to)
                )
                .groupBy(formattedDate)
                .orderBy(formattedDate.asc())
                .fetch();
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
}
