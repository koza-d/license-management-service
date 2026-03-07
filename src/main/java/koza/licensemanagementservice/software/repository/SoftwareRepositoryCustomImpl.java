package koza.licensemanagementservice.software.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import koza.licensemanagementservice.software.dto.QSoftwareDTO_SummaryResponse;
import koza.licensemanagementservice.software.dto.SoftwareDTO;
import koza.licensemanagementservice.software.entity.Software;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static koza.licensemanagementservice.software.entity.QSoftware.software;
import static koza.licensemanagementservice.license.entity.QLicense.license;
import static koza.licensemanagementservice.member.entity.QMember.member;

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

    @Override
    public Page<SoftwareDTO.SummaryResponse> findSummaryByMemberId(Long memberId, String search, Pageable pageable) {
        // 추후 Repository가 복잡해지면 조회용 SoftwareQueryRepository로 분리
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(software.member.id.eq(memberId));
        if (search != null)
            builder.and(software.name.contains(search));
        List<SoftwareDTO.SummaryResponse> content = queryFactory
                .select(new QSoftwareDTO_SummaryResponse(
                        software.id,
                        software.name,
                        software.latestVersion,
                        license.count().intValue(),
                        license.hasActiveSession.when(true).then(1L).otherwise(0L).sum().intValue(),
                        software.createAt
                ))
                .from(software)
                .leftJoin(license).on(license.software.eq(software))
                .where(builder)
                .groupBy(software.id)
                .orderBy(getOrderSpecifier(pageable.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(software.count())
                .from(software)
                .where(builder)
                .fetchOne();
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
