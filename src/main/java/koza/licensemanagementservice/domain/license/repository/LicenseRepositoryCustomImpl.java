package koza.licensemanagementservice.domain.license.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import koza.licensemanagementservice.domain.license.dto.response.LicenseAdminSummaryResponse;
import koza.licensemanagementservice.domain.license.dto.response.QLicenseAdminSummaryResponse;
import koza.licensemanagementservice.domain.license.entity.License;
import koza.licensemanagementservice.domain.license.entity.LicenseStatus;
import koza.licensemanagementservice.domain.license.repository.condition.LicenseSearchCondition;
import koza.licensemanagementservice.domain.license.repository.condition.LicenseSearchTarget;
import koza.licensemanagementservice.domain.session.repository.SessionSearchCondition;
import koza.licensemanagementservice.domain.session.dto.response.QSessionAdminResponse;
import koza.licensemanagementservice.domain.session.dto.response.SessionAdminResponse;
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
import static koza.licensemanagementservice.global.querydsl.QuerydslOrderUtil.getOrderSpecifiers;
import static org.springframework.util.StringUtils.hasText;


@RequiredArgsConstructor
public class LicenseRepositoryCustomImpl implements LicenseRepositoryCustom {
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
    public List<License> findByMemberId(Long memberId) {
        return queryFactory
                .selectFrom(license)
                .leftJoin(license.software, software)
                .leftJoin(software.member, member)
                .where(member.id.eq(memberId))
                .fetch();
    }

    @Override
    public List<License> findByExpiredWithoutLog(LocalDateTime now) {
        return queryFactory
                .selectFrom(license)
                .where(
                        license.expiredAt.before(now),
                        license.expiredLoggedAt.isNull()
                                .or(license.expiredAt.eq(license.expiredLoggedAt).not())
                )
                .fetch();
    }

    @Override
    public Page<License> findByMemberId(Long memberId, String search, Boolean hasActiveSession, Integer expireWithin, Pageable pageable) {
        List<License> content = queryFactory
                .selectFrom(license)
                .leftJoin(license.software, software)
                .leftJoin(software.member, member)
                .where(
                        license.software.member.id.eq(memberId),
                        searchNameOrMemo(search),
                        sessionFilter(hasActiveSession),
                        isExpired(expireWithin)
                )
                .orderBy(getOrderSpecifiers(pageable.getSort(), license, "id", Set.of("id", "createAt", "expiredAt")))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(license.count())
                .from(license)
                .where(
                        license.software.member.id.eq(memberId),
                        searchNameOrMemo(search),
                        sessionFilter(hasActiveSession),
                        isExpired(expireWithin)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    private static BooleanExpression isExpired(Integer expireWithin) {
        return expireWithin != null ? license.expiredAt.before(LocalDateTime.now().plusDays(expireWithin)) : null;
    }

    @Override
    public Page<License> findBySoftwareId(Long softwareId, String search, Boolean hasActiveSession, Pageable pageable) {
        List<License> content = queryFactory
                .selectFrom(license)
                .where(
                        license.software.id.eq(softwareId),
                        searchNameOrMemo(search),
                        sessionFilter(hasActiveSession)
                )
                .orderBy(getOrderSpecifiers(pageable.getSort(), license, "id", Set.of("id", "createAt", "expiredAt")))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(license.count())
                .from(license)
                .where(
                        license.software.id.eq(softwareId),
                        searchNameOrMemo(search),
                        sessionFilter(hasActiveSession)
                )
                .fetchOne();
        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    private static BooleanExpression searchNameOrMemo(String search) {
        if (!hasText(search))
            return null;

        return license.memo.containsIgnoreCase(search)
                .or(license.name.containsIgnoreCase(search));
    }

    @Override
    public Page<LicenseAdminSummaryResponse> findByAllCondition(LicenseSearchCondition condition, Pageable pageable) {
        List<LicenseAdminSummaryResponse> content = queryFactory
                .select(new QLicenseAdminSummaryResponse(
                        license.id,
                        member.email,
                        software.name,
                        license.name,
                        license.licenseKey,
                        license.createAt,
                        license.expiredAt,
                        license.hasActiveSession,
                        license.latestActiveAt,
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

    private BooleanExpression statusFilter(LicenseStatus status) {
        return status == null ? null : license.status.eq(status);
    }

    @Override
    public Page<SessionAdminResponse> findActiveSessionLicensesByCondition(SessionSearchCondition condition, Pageable pageable) {
        List<SessionAdminResponse> content = queryFactory
                .select(
                        new QSessionAdminResponse(
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

    private BooleanExpression sessionFilter(Boolean hasActiveSession) {
        return hasActiveSession != null ? license.hasActiveSession.eq(hasActiveSession) : null;
    }

    private BooleanExpression searchFilter(LicenseSearchTarget target, String search) {
        if (!hasText(search))
            return null;

        if (target != null && target != LicenseSearchTarget.ALL) {
            return switch (target) {
                case SOFTWARE_OWNER_EMAIL -> member.email.containsIgnoreCase(search);
                case SOFTWARE_NAME -> software.name.containsIgnoreCase(search);
                case LICENSE_NAME -> license.name.containsIgnoreCase(search);
                default -> null;
            };
        }

        return member.email.containsIgnoreCase(search)
                .or(software.name.containsIgnoreCase(search))
                .or(license.name.containsIgnoreCase(search));
    }
}
