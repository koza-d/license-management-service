package koza.licensemanagementservice.domain.license.log.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import koza.licensemanagementservice.domain.license.log.dto.LicenseExtendLogResponse;
import koza.licensemanagementservice.domain.license.log.dto.QLicenseExtendLogResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static koza.licensemanagementservice.domain.license.log.entity.QLicenseExtendLog.licenseExtendLog;
import static koza.licensemanagementservice.domain.member.entity.QMember.member;
import static koza.licensemanagementservice.global.querydsl.QuerydslOrderUtil.getOrderSpecifiers;

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
                .orderBy(getOrderSpecifiers(pageable.getSort(), licenseExtendLog, "id", Set.of("id", "createAt")))
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
}
