package koza.licensemanagementservice.domain.license.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import koza.licensemanagementservice.domain.license.entity.License;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static koza.licensemanagementservice.domain.license.entity.QLicense.license;
import static koza.licensemanagementservice.domain.member.entity.QMember.member;
import static koza.licensemanagementservice.domain.software.entity.QSoftware.software;


@RequiredArgsConstructor
public class LicenseRepositoryCustomImpl implements LicenseRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<License> findByIdWithSoftwareAndMember(Long licenseId) {
        // Member(Software의 연관관계) 까지 끌어오는건 해당 LicenseRepository 의 영역을 침범한 느낌이지만 일단 이대로 타협함
        return Optional.ofNullable(
                jpaQueryFactory
                        .selectFrom(license)
                        .where(license.id.eq(licenseId))
                        .join(license.software, software).fetchJoin()
                        .join(software.member, member).fetchJoin()
                        .fetchOne());
    }

    @Override
    public Optional<License> findByLicenseKeyWithSoftware(String licenseKey) {
        return Optional.ofNullable(
                jpaQueryFactory
                        .selectFrom(license)
                        .where(license.licenseKey.eq(licenseKey))
                        .leftJoin(license.software, software).fetchJoin()
                        .fetchOne());
    }

    @Override
    public List<License> findByIdInWithSoftwareWithMember(List<Long> ids) {
        return jpaQueryFactory
                .selectFrom(license)
                .where(license.id.in(ids))
                .leftJoin(license.software, software)
                .leftJoin(software.member, member)
                .fetch();
    }

    @Override
    public List<License> findByMemberId(Long memberId) {
        return jpaQueryFactory
                .selectFrom(license)
                .where(member.id.eq(memberId))
                .leftJoin(license.software, software)
                .leftJoin(software.member, member)
                .fetch()
                ;
    }

    @Override
    public Page<License> findByMemberId(Long memberId, String search, Boolean hasActiveSession, Integer expireWithin, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(license.software.member.id.eq(memberId));

        if (search != null)
            builder.and(license.name.containsIgnoreCase(search).or(license.memo.containsIgnoreCase(search)));

        if (hasActiveSession != null)
            builder.and(license.hasActiveSession.eq(hasActiveSession));

        if (expireWithin != null)
            builder.and(license.expiredAt.before(LocalDateTime.now().plusDays(expireWithin)));

        List<License> content = jpaQueryFactory
                .selectFrom(license)
                .where(builder)
                .leftJoin(license.software, software)
                .leftJoin(software.member, member)
                .orderBy(getOrderSpecifier(pageable.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = jpaQueryFactory
                .select(license.count())
                .from(license)
                .where(builder)
                .fetchOne();
        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    @Override
    public Page<License> findBySoftwareId(Long softwareId, String search, Boolean hasActiveSession, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(license.software.id.eq(softwareId));

        if (search != null)
            builder.and(license.name.containsIgnoreCase(search).or(license.memo.containsIgnoreCase(search)));

        if (hasActiveSession != null)
            builder.and(license.hasActiveSession.eq(hasActiveSession));

        List<License> content = jpaQueryFactory
                .selectFrom(license)
                .where(builder)
                .orderBy(getOrderSpecifier(pageable.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = jpaQueryFactory
                .select(license.count())
                .from(license)
                .where(builder)
                .fetchOne();
        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    private OrderSpecifier[] getOrderSpecifier(Sort sort) {
        List<OrderSpecifier> orders = new ArrayList<>();
        PathBuilder<License> entityPath = new PathBuilder<>(License.class, "license");

        for (Sort.Order order : sort) {
            Order direction = order.isAscending() ? Order.ASC : Order.DESC;
            String prop = order.getProperty();
            orders.add(new OrderSpecifier(direction, entityPath.get(prop)));
        }

        return orders.toArray(OrderSpecifier[]::new);
    }

}
