package koza.licensemanagementservice.domain.software.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import koza.licensemanagementservice.dashboard.dto.QSoftwareDailyUsage;
import koza.licensemanagementservice.dashboard.dto.QSoftwareStatsResponse;
import koza.licensemanagementservice.dashboard.dto.SoftwareStatsResponse;
import koza.licensemanagementservice.dashboard.dto.SoftwareDailyUsage;
import koza.licensemanagementservice.domain.software.dto.response.QSoftwareSummaryResponse;
import koza.licensemanagementservice.domain.software.dto.response.SoftwareSummaryResponse;
import koza.licensemanagementservice.domain.software.entity.Software;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.*;

import static koza.licensemanagementservice.domain.license.entity.QLicense.license;
import static koza.licensemanagementservice.domain.member.entity.QMember.member;
import static koza.licensemanagementservice.domain.sessionLog.entity.QSessionLog.sessionLog;
import static koza.licensemanagementservice.domain.software.entity.QSoftware.software;
import static koza.licensemanagementservice.domain.software.version.entity.QSoftwareVersion.softwareVersion;

@RequiredArgsConstructor
public class SoftwareRepositoryCustomImpl implements SoftwareRepositoryCustom {
    private final JPAQueryFactory queryFactory;

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
                .leftJoin(software).on(software.member.id.eq(member.id))
                .leftJoin(license).on(license.software.id.eq(software.id))
                .leftJoin(sessionLog).on(sessionLog.license.id.eq(license.id))
                .where(member.id.eq(memberId))
                .groupBy(software.id, software.name)
                .fetch();
    }

    @Override
    public Page<SoftwareSummaryResponse> findSummaryByMemberId(Long memberId, String search, boolean activeOnly, Pageable pageable) {
        // 추후 Repository가 복잡해지면 조회용 SoftwareQueryRepository로 분리
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(software.member.id.eq(memberId));
        if (search != null)
            builder.and(software.name.contains(search));

        JPAQuery<SoftwareSummaryResponse> query = queryFactory
                .select(new QSoftwareSummaryResponse(
                        software.id,
                        software.name,
                        softwareVersion.version,
                        license.count().intValue(),
                        license.hasActiveSession.when(true).then(1L).otherwise(0L).sum().intValue(),
                        software.createAt
                ))
                .from(software)
                .leftJoin(license).on(license.software.eq(software))
                .leftJoin(software.versions, softwareVersion).on(softwareVersion.isLatest.isTrue())
                .where(builder)
                .having(activeOnly ? license.hasActiveSession.when(true).then(1L).otherwise(0L).sum().gt(0L) : null)
                .groupBy(software.id, softwareVersion.version);

        List<SoftwareSummaryResponse> content = query
                .orderBy(getOrderSpecifier(pageable.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(software.id.countDistinct())
                .from(software);

        Long total = null;
        if (activeOnly) {
            countQuery
                    .leftJoin(license).on(license.software.eq(software))
                    .groupBy(software.id)
                    .having(license.hasActiveSession.when(true).then(1L).otherwise(0L).sum().gt(0L));
            total = (long) countQuery.fetch().size();
        } else {
            total = countQuery.where(builder).fetchOne();
        }
        return new PageImpl<>(content, pageable, total != null ? total : 0L);
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
