package koza.licensemanagementservice.verification.log.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import koza.licensemanagementservice.stat.dto.QVerificationAttemptTrend;
import koza.licensemanagementservice.stat.dto.VerificationAttemptTrend;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static koza.licensemanagementservice.verification.log.entity.QVerifyLog.verifyLog;

@RequiredArgsConstructor
public class VerifyLogRepositoryImpl implements VerifyLogRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    @Override
    public List<VerificationAttemptTrend> getVerificationMetrics(LocalDate from, LocalDate to) {
        StringTemplate formattedDate = Expressions.stringTemplate(
                "DATE_FORMAT({0}, '%Y-%m-%d')",
                verifyLog.createAt);

        return queryFactory
                .select(new QVerificationAttemptTrend(
                        formattedDate,
                        verifyLog.count(),
                        verifyLog.isSuccess.when(true).then(1L).otherwise(Expressions.nullExpression()).count(),
                        verifyLog.isSuccess.when(false).then(1L).otherwise(Expressions.nullExpression()).count()
                        )
                )
                .from(verifyLog)
                .where(
                        createAtBetween(from, to)
                )
                .groupBy(formattedDate)
                .orderBy(formattedDate.asc())
                .fetch();
    }

    private static BooleanExpression createAtBetween(LocalDate from, LocalDate to) {
        if (from == null && to == null) return null;

        if (to == null)
            return verifyLog.createAt.goe(from.atStartOfDay());

        if (from == null)
            return verifyLog.createAt.loe(to.atTime(LocalTime.MAX));

        return verifyLog.createAt.between(from.atStartOfDay(), to.atTime(LocalTime.MAX));
    }
}
