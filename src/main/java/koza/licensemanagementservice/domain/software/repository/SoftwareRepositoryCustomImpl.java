package koza.licensemanagementservice.domain.software.repository;

import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import koza.licensemanagementservice.dashboard.dto.QSoftwareDailyUsage;
import koza.licensemanagementservice.dashboard.dto.QSoftwareStatsResponse;
import koza.licensemanagementservice.dashboard.dto.SoftwareStatsResponse;
import koza.licensemanagementservice.dashboard.dto.SoftwareDailyUsage;
import koza.licensemanagementservice.domain.software.dto.response.*;
import koza.licensemanagementservice.domain.software.entity.Software;
import koza.licensemanagementservice.domain.software.entity.SoftwareStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static koza.licensemanagementservice.domain.license.entity.QLicense.license;
import static koza.licensemanagementservice.domain.member.entity.QMember.member;
import static koza.licensemanagementservice.domain.session.log.entity.QSessionLog.sessionLog;
import static koza.licensemanagementservice.domain.software.entity.QSoftware.software;
import static koza.licensemanagementservice.domain.software.version.entity.QSoftwareVersion.softwareVersion;

@RequiredArgsConstructor
public class SoftwareRepositoryCustomImpl implements SoftwareRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public SoftwareAdminDetailResponse findBySoftwareId(Long softwareId) {
        return queryFactory
                .select(
                        new QSoftwareAdminDetailResponse(
                                software.id,
                                member.email,
                                member.nickname,
                                software.name,
                                softwareVersion.version,
                                software.apiKey,
                                licenseCount(),
                                software.limitLicense,
                                software.globalVariables,
                                software.localVariables,
                                software.createAt
                        )
                )
                .from(software)
                .where(
                        software.id.eq(softwareId)
                )
                .leftJoin(software.member, member)
                .leftJoin(softwareVersion).on(
                        softwareVersion.software.eq(software),
                        softwareVersion.isLatest.isTrue()
                )
                .fetchOne();
    }

    @Override
    public Optional<Software> findByIdWithMember(Long softwareId) {
        return Optional.ofNullable(queryFactory
                .selectFrom(software)
                .where(software.id.eq(softwareId))
                .innerJoin(software.member, member).fetchJoin()
                .fetchOne());
    }

    /*
     * 회원이 소유한 전체 라이센스에서 발생한 세션로그에서 사용 시간을 가져오는 함수
     * { softwareId:1, date:2026-03-04, minutes:103 },
     * { softwareId:1, date:2026-03-05, minutes:53 },
     * { softwareId:1, date:2026-03-06, minutes:34 },
     * { softwareId:2, date:2026-03-02, minutes:44 },
     * { softwareId:2, date:2026-03-03, minutes:52 }
     *                      .
     *                      .
     *                      .
     * */
    @Override
    public List<SoftwareDailyUsage> findDailyUsageByMemberId(Long memberId, LocalDateTime startDate) {

        NumberTemplate<Long> diffMinutes = Expressions.numberTemplate(
                Long.class,
                "TIMESTAMPDIFF(MINUTE, {0}, {1})",
                sessionLog.verifyAt,
                sessionLog.releaseAt
        );

        // LocalDate 변환이 안돼서 java.sql.Date 명시 -> DTO 생성자에서 LocalDate 로 변경
        DateTemplate<java.sql.Date> dateOnly = Expressions.dateTemplate(
                java.sql.Date.class,
                "DATE({0})",
                sessionLog.verifyAt);

        return queryFactory
                .select(
                        new QSoftwareDailyUsage(
                                software.id,
                                dateOnly,
                                diffMinutes.sum()
                        )
                )
                .from(member)
                .join(software).on(software.member.id.eq(member.id))
                .join(license).on(license.software.id.eq(software.id))
                .join(sessionLog).on(sessionLog.license.id.eq(license.id))
                .where(member.id.eq(memberId)
                        .and(sessionLog.verifyAt.goe(startDate))
                )
                .groupBy(software.id, dateOnly)
                .orderBy(new OrderSpecifier<>(Order.ASC, dateOnly))
                .fetch()
                ;
    }

    /*
     * 회원이 소유한 전체 라이센스의 총 사용시간, 라이센스 상태에 따른 갯수를 가져오는 함수
     * */
    @Override
    public List<SoftwareStatsResponse> findSoftwareStatsByMemberId(Long memberId) {
        NumberTemplate<Long> diffMinutes = Expressions.numberTemplate(
                Long.class,
                "TIMESTAMPDIFF(MINUTE, {0}, {1})",
                sessionLog.verifyAt,
                sessionLog.releaseAt
        );

        NumberExpression<Long> activeSessions = new CaseBuilder()
                .when(license.hasActiveSession.isTrue())
                .then(license.id)
                .otherwise((Long) null)
                .countDistinct();

        return queryFactory
                .select(
                        new QSoftwareStatsResponse(
                                software.id,
                                software.name,
                                license.id.countDistinct(), // 라이센스 총 개수
                                activeSessions, // 세션 활성화 된 라이센스 수
                                diffMinutes.sum() // 소프트웨어 하위 세션 사용 시간 전체 합
                        )
                )
                .from(member)
                .leftJoin(software).on(
                        software.member.id.eq(member.id)
                )
                .leftJoin(license).on(
                        license.software.id.eq(software.id)
                )
                .leftJoin(sessionLog).on(
                        sessionLog.license.id.eq(license.id)
                )
                .where(member.id.eq(memberId))
                .groupBy(software.id, software.name)
                .fetch();
    }

    @Override
    public Page<SoftwareSummaryResponse> findSummaryByMemberId(Long memberId, String search, boolean activeOnly, Pageable pageable) {
        List<SoftwareSummaryResponse> content = queryFactory
                .select(
                        new QSoftwareSummaryResponse(
                                software.id,
                                software.name,
                                softwareVersion.version,
                                licenseCount(),
                                activeSessionCount(),
                                software.createAt
                        )
                )
                .from(software)
                .leftJoin(softwareVersion).on(
                        softwareVersion.software.eq(software),
                        softwareVersion.isLatest.isTrue()
                )
                .where(
                        software.member.id.eq(memberId),
                        containsName(search),
                        activeSessionOnlyFilter(activeOnly)
                )
                .orderBy(getOrderSpecifier(pageable.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(software.count())
                .from(software)
                .where(
                        software.member.id.eq(memberId),
                        containsName(search),
                        activeSessionOnlyFilter(activeOnly)
                ).fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    @Override
    public Page<SoftwareAdminSummaryResponse> searchSoftwareByCondition(SoftwareAdminSearchCondition condition, Pageable pageable) {
        List<SoftwareAdminSummaryResponse> content = queryFactory
                .select(
                        new QSoftwareAdminSummaryResponse(
                                software.id,
                                software.name,
                                softwareVersion.version,
                                member.email,
                                software.status,
                                software.createAt
                        )
                )
                .from(software)
                .leftJoin(softwareVersion).on(
                        softwareVersion.software.eq(software),
                        softwareVersion.isLatest.isTrue()
                )
                .leftJoin(software.member, member)
                .where(
                        searchFilter(condition.getTarget(), condition.getSearch()),
                        createAtBetween(condition.getFrom(), condition.getTo()),
                        statusFilter(condition.getStatus())
                )
                .orderBy(getOrderSpecifier(pageable.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(software.count())
                .from(software)
                .leftJoin(softwareVersion).on(
                        softwareVersion.software.eq(software),
                        softwareVersion.isLatest.isTrue()
                )
                .leftJoin(software.member, member)
                .where(
                        searchFilter(condition.getTarget(), condition.getSearch()),
                        createAtBetween(condition.getFrom(), condition.getTo()),
                        statusFilter(condition.getStatus())
                ).fetchOne();
        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    @Override
    public SoftwareAdminStatsResponse getSoftwareUsageStat(Long softwareId) {
        NumberTemplate<Long> diffSeconds = Expressions.numberTemplate(
                Long.class,
                "TIMESTAMPDIFF(SECOND, {0}, {1})",
                sessionLog.verifyAt,
                sessionLog.releaseAt
        );

        return queryFactory
                .select(
                        new QSoftwareAdminStatsResponse(
                                diffSeconds.sum().longValue(),
                                sessionLog.count(),
                                diffSeconds.avg().longValue()
                        )
                )
                .from(sessionLog)
                .join(sessionLog.license, license)
                .where(
                        license.software.id.eq(softwareId)
                )
                .fetchOne();
    }

    private BooleanExpression searchFilter(SoftwareAdminSearchTarget target, String search) {
        if (search == null || search.isEmpty())
            return null;

        if (target != null && target != SoftwareAdminSearchTarget.ALL) {
            return switch (target) {
                case SOFTWARE_NAME -> software.name.containsIgnoreCase(search);
                case OWNER_EMAIL -> member.email.containsIgnoreCase(search);
                default -> null;
            };
        }

        return software.name.containsIgnoreCase(search)
                .or(member.email.containsIgnoreCase(search));
    }

    private BooleanExpression createAtBetween(LocalDate from, LocalDate to) {
        if (from == null && to == null) return null;

        if (to == null)
            return software.createAt.goe(from.atStartOfDay());

        if (from == null)
            return software.createAt.loe(to.atTime(LocalTime.MAX));

        return software.createAt.between(from.atStartOfDay(), to.atTime(LocalTime.MAX));
    }

    private BooleanExpression statusFilter(SoftwareStatus status) {
        return status == null ? null : software.status.eq(status);
    }

    private Expression<Integer> activeSessionCount() {
        return ExpressionUtils.as(
                JPAExpressions.select(license.count().intValue())
                        .from(license)
                        .where(license.software.eq(software), license.hasActiveSession.isTrue()),
                "activeSessionCount"
        );
    }

    private Expression<Integer> licenseCount() {
        return ExpressionUtils.as(
                JPAExpressions.select(license.count().intValue())
                        .from(license)
                        .where(license.software.eq(software)),
                "licenseCount"
        );
    }

    private BooleanExpression activeSessionOnlyFilter(boolean activeOnly) {
        BooleanExpression exists = JPAExpressions.selectOne()
                .from(license)
                .where(license.software.eq(software), license.hasActiveSession.isTrue())
                .exists();
        return activeOnly ? exists : null;
    }

    private BooleanExpression containsName(String search) {
        return search != null ? software.name.containsIgnoreCase(search) : null;
    }

    private OrderSpecifier[] getOrderSpecifier(Sort sort) {
        List<OrderSpecifier> orders = new ArrayList<>();
        PathBuilder<Software> entityPath = new PathBuilder<>(Software.class, "software");

        for (Sort.Order order : sort) {
            Order direction = order.isAscending() ? Order.ASC : Order.DESC;
            String prop = order.getProperty();
            orders.add(new OrderSpecifier(direction, entityPath.get(prop)));
        }

        return orders.toArray(OrderSpecifier[]::new);
    }
}
