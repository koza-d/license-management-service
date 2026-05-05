package koza.licensemanagementservice.domain.member.repository;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import koza.licensemanagementservice.domain.member.entity.MemberGrade;
import koza.licensemanagementservice.domain.member.entity.MemberStatus;
import koza.licensemanagementservice.stat.dto.response.MemberPlanDistributionResponse;
import koza.licensemanagementservice.stat.dto.response.QMemberPlanDistributionResponse;
import lombok.RequiredArgsConstructor;

import static koza.licensemanagementservice.domain.member.entity.QMember.member;

@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public MemberPlanDistributionResponse getMemberPlanDistribution() {
        return queryFactory
                .select(
                        new QMemberPlanDistributionResponse(
                                member.grade.when(MemberGrade.BASIC).then(1L).otherwise(Expressions.nullExpression()).count(),
                                member.grade.when(MemberGrade.PREMIUM).then(1L).otherwise(Expressions.nullExpression()).count(),
                                member.grade.when(MemberGrade.ENTERPRISE).then(1L).otherwise(Expressions.nullExpression()).count()
                        )
                )
                .from(member)
                .where(
                        member.status.eq(MemberStatus.ACTIVE)
                )
                .fetchOne();
    }
}
