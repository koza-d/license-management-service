package koza.licensemanagementservice.dashboard.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import koza.licensemanagementservice.dashboard.dto.response.AdminDashboardLicenseStatsResponse;
import koza.licensemanagementservice.dashboard.dto.response.AdminStatsResponse;
import koza.licensemanagementservice.dashboard.dto.response.QAdminDashboardLicenseStatsResponse;
import koza.licensemanagementservice.dashboard.dto.response.QAdminStatsResponse;
import koza.licensemanagementservice.domain.license.entity.LicenseStatus;
import koza.licensemanagementservice.domain.license.repository.LicenseExpressions;
import koza.licensemanagementservice.domain.member.entity.MemberStatus;
import koza.licensemanagementservice.domain.qna.entity.QnaStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static koza.licensemanagementservice.domain.license.entity.QLicense.license;
import static koza.licensemanagementservice.domain.member.entity.QMember.member;
import static koza.licensemanagementservice.domain.qna.entity.QQna.qna;
import static koza.licensemanagementservice.domain.software.entity.QSoftware.software;
import static koza.licensemanagementservice.global.querydsl.QuerydslOrderUtil.countWhen;

@Repository
@RequiredArgsConstructor
public class DashboardRepositoryImpl implements DashboardRepository {
    private final JPAQueryFactory queryFactory;
    @Override
    public AdminStatsResponse getAdminStats() {
        return queryFactory
                .select(new QAdminStatsResponse(
                        member.count(),
                        JPAExpressions.select(software.count()).from(software),
                        JPAExpressions
                                .select(license.count())
                                .from(license)
                                .where(license.hasActiveSession.isTrue()),
                        JPAExpressions
                                .select(qna.count())
                                .from(qna)
                                .where(qna.status.eq(QnaStatus.PENDING))
                ))
                .from(member)
                .where(member.status.ne(MemberStatus.WITHDRAW))
                .fetchOne();
    }

    @Override
    public AdminDashboardLicenseStatsResponse getAdminLicenseStats() {
        return queryFactory
                .select(
                        new QAdminDashboardLicenseStatsResponse(
                                license.count(),
                                countWhen(LicenseExpressions.isAllocated()),
                                countWhen(LicenseExpressions.isUnAllocated()),
                                countWhen(license.status.eq(LicenseStatus.INACTIVE)),
                                countWhen(license.status.eq(LicenseStatus.EXPIRED)),
                                countWhen(license.hasActiveSession.isTrue())
                        )
                )
                .from(license)
                .fetchOne();
    }
}
