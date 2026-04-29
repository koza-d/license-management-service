package koza.licensemanagementservice.sdk.log.repository;

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

import static koza.licensemanagementservice.sdk.log.entity.QSdkLog.sdkLog;

@RequiredArgsConstructor
public class SdkLogRepositoryImpl implements SdkLogRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    @Override
    public List<VerificationAttemptTrend> getVerificationMetrics(LocalDate from, LocalDate to) {
        StringTemplate formattedDate = Expressions.stringTemplate(
                "DATE_FORMAT({0}, '%Y-%m-%d')",
                sdkLog.createAt);

        return queryFactory
                .select(new QVerificationAttemptTrend(
                        formattedDate,
                        sdkLog.count(),
                        sdkLog.isSuccess.when(true).then(1L).otherwise(Expressions.nullExpression()).count(),
                        sdkLog.isSuccess.when(false).then(1L).otherwise(Expressions.nullExpression()).count()
                        )
                )
                .from(sdkLog)
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
            return sdkLog.createAt.goe(from.atStartOfDay());

        if (from == null)
            return sdkLog.createAt.loe(to.atTime(LocalTime.MAX));

        return sdkLog.createAt.between(from.atStartOfDay(), to.atTime(LocalTime.MAX));
    }
}
