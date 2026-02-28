package koza.licensemanagementservice.software.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import koza.licensemanagementservice.software.dto.QSoftwareDTO_SummaryResponse;
import koza.licensemanagementservice.software.dto.SoftwareDTO;
import koza.licensemanagementservice.software.entity.Software;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

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
    public Page<SoftwareDTO.SummaryResponse> findSummaryByMemberId(Long memberId, Pageable pageable) {
        // 추후 Repository가 복잡해지면 조회용 SoftwareQueryRepository로 분리
        //
        List<SoftwareDTO.SummaryResponse> content = queryFactory
                .select(new QSoftwareDTO_SummaryResponse(
                        software.id,
                        software.name,
                        software.version,
                        license.count().intValue(),
                        software.createAt
                ))
                .from(software)
                .leftJoin(license).on(license.software.eq(software))
                .where(software.member.id.eq(memberId))
                .groupBy(software.id)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(software.count())
                .from(software)
                .where(software.member.id.eq(memberId))
                .fetchOne();
        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }
}
