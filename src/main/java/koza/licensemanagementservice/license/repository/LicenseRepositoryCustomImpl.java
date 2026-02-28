package koza.licensemanagementservice.license.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import koza.licensemanagementservice.license.entity.License;
import koza.licensemanagementservice.license.entity.QLicense;
import koza.licensemanagementservice.member.entity.QMember;
import koza.licensemanagementservice.software.entity.QSoftware;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

import static koza.licensemanagementservice.license.entity.QLicense.license;
import static koza.licensemanagementservice.member.entity.QMember.member;
import static koza.licensemanagementservice.software.entity.QSoftware.software;

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
    public List<License> findByIdInWithSoftwareWithMember(List<Long> ids) {
        return jpaQueryFactory
                .selectFrom(license)
                .where(license.id.in(ids))
                .leftJoin(license.software, software)
                .leftJoin(software.member, member)
                .fetch();
    }
}
