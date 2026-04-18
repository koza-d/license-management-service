package koza.licensemanagementservice.domain.member.log.repository;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import koza.licensemanagementservice.domain.member.log.entity.MemberLogType;
import koza.licensemanagementservice.stat.dto.MemberTrendResponse;
import koza.licensemanagementservice.stat.dto.QMemberTrendResponse;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static koza.licensemanagementservice.domain.member.log.entity.QMemberLog.memberLog;


@RequiredArgsConstructor
public class MemberLogRepositoryImpl implements MemberLogRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<MemberTrendResponse> getMemberFlowTrend(LocalDate from, LocalDate to) {
        StringTemplate formattedDate = Expressions.stringTemplate(
                "DATE_FORMAT({0}, '%Y-%m-%d')",
                memberLog.createAt);

        return queryFactory
                .select(
                        new QMemberTrendResponse(
                                formattedDate,
                                memberLog.logType.when(MemberLogType.JOIN).then(1L).otherwise(Expressions.nullExpression()).count(),
                                memberLog.logType.when(MemberLogType.WITHDRAW).then(1L).otherwise(Expressions.nullExpression()).count()
                        )
                )
                .from(memberLog)
                .where(
                        memberLog.createAt.between(from.atStartOfDay(), to.atTime(LocalTime.MAX)),
                        // 가입 또는 탈퇴 로그만 필터링 (불필요한 로그인 로그 등 제외)
                        memberLog.logType.in(MemberLogType.JOIN, MemberLogType.WITHDRAW)
                )
                .groupBy(formattedDate)
                .orderBy(formattedDate.asc())
                .fetch();
    }
}
