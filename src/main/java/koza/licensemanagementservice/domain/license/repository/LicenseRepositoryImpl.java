package koza.licensemanagementservice.domain.license.repository;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import koza.licensemanagementservice.dashboard.dto.response.DashboardLicenseStatsResponse;
import koza.licensemanagementservice.dashboard.dto.response.ExpiringLicenseResponse;
import koza.licensemanagementservice.dashboard.dto.response.QDashboardLicenseStatsResponse;
import koza.licensemanagementservice.dashboard.dto.response.QExpiringLicenseResponse;
import koza.licensemanagementservice.domain.license.dto.condition.LicenseSearchCondition;
import koza.licensemanagementservice.domain.license.dto.condition.LicenseSearchTarget;
import koza.licensemanagementservice.domain.license.dto.response.*;
import koza.licensemanagementservice.domain.license.entity.License;
import koza.licensemanagementservice.domain.license.entity.LicenseStatus;
import koza.licensemanagementservice.domain.license.dto.condition.AdminLicenseSearchCondition;
import koza.licensemanagementservice.domain.license.dto.condition.AdminLicenseSearchTarget;
import koza.licensemanagementservice.domain.session.dto.response.AdminSessionResponse;
import koza.licensemanagementservice.domain.session.dto.condition.SessionSearchCondition;
import koza.licensemanagementservice.domain.session.dto.response.QAdminSessionResponse;
import koza.licensemanagementservice.domain.session.repository.SessionSearchTarget;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static koza.licensemanagementservice.domain.license.entity.QLicense.license;
import static koza.licensemanagementservice.domain.member.entity.QMember.member;
import static koza.licensemanagementservice.domain.software.entity.QSoftware.software;
import static koza.licensemanagementservice.global.querydsl.QuerydslOrderUtil.countWhen;
import static koza.licensemanagementservice.global.querydsl.QuerydslOrderUtil.getOrderSpecifiers;
import static org.springframework.util.StringUtils.hasText;


@RequiredArgsConstructor
public class LicenseRepositoryImpl implements LicenseRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<License> findByIdWithSoftwareAndMember(Long licenseId) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(license)
                        .join(license.software, software).fetchJoin()
                        .join(software.member, member).fetchJoin()
                        .where(license.id.eq(licenseId))
                        .fetchOne());
    }

    @Override
    public Optional<License> findByLicenseKeyWithSoftware(String licenseKey) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(license)
                        .leftJoin(license.software, software).fetchJoin()
                        .where(license.licenseKey.eq(licenseKey))
                        .fetchOne());
    }

    @Override
    public List<License> findByIdInWithSoftwareWithMember(List<Long> ids) {
        return queryFactory
                .selectFrom(license)
                .leftJoin(license.software, software)
                .leftJoin(software.member, member)
                .where(license.id.in(ids))
                .fetch();
    }

    @Override
    public Page<AdminLicenseSummaryResponse> findByAllCondition(AdminLicenseSearchCondition condition, Pageable pageable) {
        List<AdminLicenseSummaryResponse> content = queryFactory
                .select(new QAdminLicenseSummaryResponse(
                        license.id,
                        member.email,
                        software.name,
                        license.name,
                        license.licenseKey,
                        license.createAt,
                        license.expiredAt,
                        license.hasActiveSession,
                        license.latestActiveAt,
                        license.startDurationDays,
                        license.status.stringValue()
                ))
                .from(license)
                .leftJoin(license.software, software)
                .leftJoin(software.member, member)
                .where(
                        searchFilter(condition.getTarget(), condition.getSearch()),
                        sessionFilter(condition.getHasActiveSession()),
                        statusFilter(condition.getStatus())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderSpecifiers(pageable.getSort(), license, "id", Set.of("id", "createAt", "expiredAt")))
                .fetch();

        Long total = queryFactory
                .select(license.count())
                .from(license)
                .leftJoin(license.software, software)
                .leftJoin(software.member, member)
                .where(
                        searchFilter(condition.getTarget(), condition.getSearch()),
                        sessionFilter(condition.getHasActiveSession()),
                        statusFilter(condition.getStatus())
                ).fetchOne();
        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    @Override
    public Page<License> searchLicensesByMemberId(Long memberId, LicenseSearchCondition condition, Pageable pageable) {
        List<License> content = queryFactory
                .selectFrom(license)
                .innerJoin(license.software, software)
                .where(
                        software.member.id.eq(memberId),
                        softwareFilter(condition.getSoftwareId()),
                        searchFilter(condition.getTarget(), condition.getSearch()),
                        sessionFilter(condition.getHasActiveSession()),
                        statusFilter(condition.getStatus())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderSpecifiers(pageable.getSort(), license, "id", Set.of("createAt", "expiredAt")))
                .fetch();

        Long total = queryFactory
                .select(license.count())
                .from(license)
                .innerJoin(license.software, software)
                .where(
                        software.member.id.eq(memberId),
                        softwareFilter(condition.getSoftwareId()),
                        searchFilter(condition.getTarget(), condition.getSearch()),
                        sessionFilter(condition.getHasActiveSession()),
                        statusFilter(condition.getStatus())
                )
                .fetchOne();
        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    private BooleanExpression softwareFilter(Long softwareId) {
        return softwareId == null ? null : software.id.eq(softwareId);
    }

    private BooleanExpression statusFilter(LicenseStatus status) {
        return status == null ? null : license.status.eq(status);
    }

    @Override
    public Page<AdminSessionResponse> findActiveSessionLicensesByCondition(SessionSearchCondition condition, Pageable pageable) {
        List<AdminSessionResponse> content = queryFactory
                .select(
                        new QAdminSessionResponse(
                                license.id,
                                license.licenseKey,
                                license.name,
                                member.email,
                                software.name,
                                license.latestActiveAt
                        )
                )
                .from(license)
                .leftJoin(license.software, software)
                .leftJoin(software.member, member)
                .where(
                        license.hasActiveSession.isTrue(),
                        searchSessionCondition(condition)
                )
                .orderBy(getOrderSpecifiers(pageable.getSort(), license, "latestActiveAt", Set.of("latestActiveAt")))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(license.count())
                .from(license)
                .leftJoin(license.software, software)
                .leftJoin(software.member, member)
                .where(
                        license.hasActiveSession.isTrue(),
                        searchSessionCondition(condition)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    @Override
    public List<License> bulkUpdateExpiredStatus(LocalDateTime now) {
        List<License> targets = queryFactory
                .selectFrom(license)
                .where(
                        license.status.eq(LicenseStatus.ACTIVE),
                        license.expiredAt.before(now)
                )
                .fetch();

        if (!targets.isEmpty()) {
            queryFactory
                    .update(license)
                    .set(license.status, LicenseStatus.EXPIRED)
                    .where(license.in(targets))
                    .execute();
        }
        return targets;
    }

    @Override
    public List<License> bulkTransitionStatus(LicenseStatus from, LicenseStatus to, LocalDateTime now) {
        List<License> targets = queryFactory
                .selectFrom(license)
                .where(
                        license.status.eq(from),
                        license.statusUntil.before(now)
                )
                .fetch();

        if (!targets.isEmpty()) {
            queryFactory
                    .update(license)
                    .set(license.status, to)
                    .where(license.in(targets))
                    .execute();
        }
        return targets;
    }

    private BooleanExpression searchSessionCondition(SessionSearchCondition condition) {
        String search = condition.getSearch();
        if (!hasText(search))
            return null;

        SessionSearchTarget target = condition.getTarget();
        if (target != null && target != SessionSearchTarget.ALL) {
            return switch (target) {
                case OWNER_EMAIL -> member.email.containsIgnoreCase(search);
                case SOFTWARE_NAME -> software.name.containsIgnoreCase(search);
                case LICENSE_NAME -> license.name.containsIgnoreCase(search);
                case LICENSE_KEY -> license.licenseKey.containsIgnoreCase(search);
                default -> null;
            };
        }
        return member.email.containsIgnoreCase(search)
                .or(software.name.containsIgnoreCase(search))
                .or(license.name.containsIgnoreCase(search))
                .or(license.licenseKey.containsIgnoreCase(search));
    }

    @Override
    public List<ExpiringLicenseResponse> findExpiringSoonLicensesByMember(Long memberId, LocalDateTime now, int limit) {
        return queryFactory
                .select(new QExpiringLicenseResponse(
                        license.id,
                        license.name,
                        license.licenseKey,
                        member.nickname,
                        software.name,
                        license.expiredAt
                ))
                .from(license)
                .join(license.software, software)
                .join(software.member, member)
                .where(software.member.id.eq(memberId)
                        .and(license.status.eq(LicenseStatus.ACTIVE))
                        .and(license.expiredAt.goe(now)))
                .orderBy(license.expiredAt.asc())
                .limit(limit)
                .fetch();
    }

    @Override
    public List<License> findActiveSessionLicensesByMember(Long memberId, int limit) {
        return queryFactory
                .selectFrom(license)
                .join(license.software, software).fetchJoin()
                .join(software.member, member).fetchJoin()
                .where(software.member.id.eq(memberId)
                        .and(license.hasActiveSession.isTrue()))
                .orderBy(license.latestActiveAt.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public LicenseStatsResponse getLicenseStatsBySoftwareId(Long softwareId) {
        return queryFactory
                .select(
                        new QLicenseStatsResponse(
                                license.count(),
                                countWhen(license.status.eq(LicenseStatus.EXPIRED)),
                                countWhen(license.status.eq(LicenseStatus.INACTIVE)),
                                countWhen(license.status.eq(LicenseStatus.ACTIVE)),
                                countWhen(license.status.eq(LicenseStatus.BANNED)),
                                countWhen(license.hasActiveSession.isTrue())
                        )
                )
                .from(license)
                .where(license.software.id.eq(softwareId))
                .fetchOne();
    }

    @Override
    public DashboardLicenseStatsResponse getLicenseStatsByMemberId(Long memberId) {
        BooleanExpression isTemporaryBanned = license.status.eq(LicenseStatus.BANNED).and(license.statusUntil.isNotNull());
        BooleanExpression isPermanentBanned = license.status.eq(LicenseStatus.BANNED).and(license.statusUntil.isNull());

        return queryFactory
                .select(
                        new QDashboardLicenseStatsResponse(
                                license.count(),
                                countWhen(LicenseExpressions.isAllocated()),
                                countWhen(license.status.eq(LicenseStatus.ACTIVE)),
                                countWhen(isTemporaryBanned),
                                countWhen(LicenseExpressions.isUnAllocated()),
                                countWhen(license.status.eq(LicenseStatus.INACTIVE)),
                                countWhen(license.status.eq(LicenseStatus.EXPIRED)),
                                countWhen(isPermanentBanned),
                                countWhen(license.hasActiveSession.isTrue())
                        )
                )
                .from(license)
                .innerJoin(license.software, software)
                .where(software.member.id.eq(memberId))
                .fetchOne();
    }

    private BooleanExpression sessionFilter(Boolean hasActiveSession) {
        return hasActiveSession != null ? license.hasActiveSession.eq(hasActiveSession) : null;
    }

    private BooleanExpression searchFilter(LicenseSearchTarget target, String search) {
        if (!hasText(search))
            return null;

        if (target != null && target != LicenseSearchTarget.ALL) {
            return switch (target) {
                case LICENSE_MEMO -> license.memo.containsIgnoreCase(search);
                case LICENSE_NAME -> license.name.containsIgnoreCase(search);
                case LICENSE_KEY -> license.licenseKey.containsIgnoreCase(search);
                default -> null;
            };
        }

        return license.memo.containsIgnoreCase(search)
                .or(license.name.containsIgnoreCase(search))
                .or(license.licenseKey.containsIgnoreCase(search));
    }

    private BooleanExpression searchFilter(AdminLicenseSearchTarget target, String search) {
        if (!hasText(search))
            return null;

        if (target != null && target != AdminLicenseSearchTarget.ALL) {
            return switch (target) {
                case SOFTWARE_OWNER_EMAIL -> member.email.containsIgnoreCase(search);
                case SOFTWARE_NAME -> software.name.containsIgnoreCase(search);
                case LICENSE_NAME -> license.name.containsIgnoreCase(search);
                case LICENSE_KEY -> license.licenseKey.containsIgnoreCase(search);
                default -> null;
            };
        }

        return member.email.containsIgnoreCase(search)
                .or(software.name.containsIgnoreCase(search))
                .or(license.name.containsIgnoreCase(search))
                .or(license.licenseKey.containsIgnoreCase(search));
    }
}
