package koza.licensemanagementservice.domain.qna.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import koza.licensemanagementservice.dashboard.dto.response.PendingQnaResponse;
import koza.licensemanagementservice.dashboard.dto.response.QPendingQnaResponse;
import koza.licensemanagementservice.domain.qna.dto.request.QnaAdminSearchCondition;
import koza.licensemanagementservice.domain.qna.dto.response.AdminQnaSummaryResponse;
import koza.licensemanagementservice.domain.qna.dto.response.QnaListResponse;
import koza.licensemanagementservice.domain.qna.entity.QnaPriority;
import koza.licensemanagementservice.domain.qna.entity.QnaStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

import static koza.licensemanagementservice.domain.member.entity.QMember.member;
import static koza.licensemanagementservice.domain.qna.entity.QQna.qna;
import static koza.licensemanagementservice.domain.software.entity.QSoftware.software;

@RequiredArgsConstructor
public class QnaRepositoryImpl implements QnaRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<QnaListResponse> findAllQuestions(String search, QnaStatus status, Pageable pageable) {
        return findQuestions(null, null, search, status, pageable);
    }

    @Override
    public Page<QnaListResponse> findBySoftwareId(Long softwareId, String search, QnaStatus status, Pageable pageable) {
        return findQuestions(null, softwareId, search, status, pageable);
    }

    @Override
    public Page<QnaListResponse> findMyQuestions(Long memberId, Long softwareId, String search, QnaStatus status, Pageable pageable) {
        return findQuestions(memberId, softwareId, search, status, pageable);
    }

    private Page<QnaListResponse> findQuestions(Long memberId, Long softwareId, String search, QnaStatus status, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();

        if (memberId != null) {
            builder.and(qna.member.id.eq(memberId));
        }
        if (softwareId != null) {
            builder.and(qna.software.id.eq(softwareId));
        }
        if (status != null) {
            builder.and(qna.status.eq(status));
        }
        if (search != null && !search.isBlank()) {
            builder.and(
                    qna.software.name.containsIgnoreCase(search)
                            .or(qna.title.containsIgnoreCase(search))
                            .or(qna.content.containsIgnoreCase(search))
            );
        }

        List<QnaListResponse> content = queryFactory
                .select(Projections.constructor(QnaListResponse.class,
                        qna.id,
                        qna.software.name,
                        qna.nickname,
                        qna.title,
                        qna.status,
                        qna.priority,
                        qna.createAt
                ))
                .from(qna)
                .where(builder)
                .orderBy(toOrderSpecifiers(pageable.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(qna.count())
                .from(qna)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    @Override
    public Page<AdminQnaSummaryResponse> findByAdminCondition(QnaAdminSearchCondition condition, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();

        if (condition.getStatus() != null && !condition.getStatus().isEmpty()) {
            builder.and(qna.status.in(condition.getStatus()));
        }

        if (condition.getPriority() != null && !condition.getPriority().isEmpty()) {
            builder.and(qna.priority.in(condition.getPriority()));
        }

        if (condition.hasAnyFieldFilter()) {
            if (condition.getTitle() != null)
                builder.and(qna.title.containsIgnoreCase(condition.getTitle()));
            if (condition.getAuthorEmail() != null)
                builder.and(member.email.containsIgnoreCase(condition.getAuthorEmail()));
            if (condition.getSoftwareName() != null)
                builder.and(software.name.containsIgnoreCase(condition.getSoftwareName()));
        } else if (condition.hasFullTextFilter()) {
            String q = condition.getQ();
            builder.and(
                    qna.title.containsIgnoreCase(q)
                            .or(member.email.containsIgnoreCase(q))
                            .or(software.name.containsIgnoreCase(q))
            );
        }

        if (condition.getCreatedAfter() != null)
            builder.and(qna.createAt.goe(condition.getCreatedAfter()));
        if (condition.getCreatedBefore() != null)
            builder.and(qna.createAt.loe(condition.getCreatedBefore()));
        if (condition.getAnsweredAfter() != null)
            builder.and(qna.answeredAt.goe(condition.getAnsweredAfter()));
        if (condition.getAnsweredBefore() != null)
            builder.and(qna.answeredAt.loe(condition.getAnsweredBefore()));

        List<AdminQnaSummaryResponse> content = queryFactory
                .select(Projections.constructor(AdminQnaSummaryResponse.class,
                        qna.id,
                        qna.title,
                        member.email,
                        software.name,
                        qna.status,
                        qna.priority,
                        qna.createAt,
                        qna.answeredAt
                ))
                .from(qna)
                .leftJoin(qna.software, software)
                .leftJoin(qna.member, member)
                .where(builder)
                .orderBy(toOrderSpecifiers(pageable.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(qna.count())
                .from(qna)
                .leftJoin(qna.software, software)
                .leftJoin(qna.member, member)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    @Override
    public List<PendingQnaResponse> findPendingForDashboard(int limit) {
        // URGENT=0, NORMAL=1 → ASC 정렬 시 URGENT 먼저.
        // 문자열 사전순에 의존하지 않아 추후 LOW/HIGH 같은 단계 추가에도 강건.
        NumberExpression<Integer> priorityRank = new CaseBuilder()
                .when(qna.priority.eq(QnaPriority.URGENT)).then(0)
                .otherwise(1);

        return queryFactory
                .select(new QPendingQnaResponse(
                        qna.id,
                        qna.title,
                        software.name,
                        member.email,
                        qna.priority,
                        qna.createAt))
                .from(qna)
                .innerJoin(qna.software, software)
                .innerJoin(qna.member, member)
                .where(qna.status.eq(QnaStatus.PENDING))
                .orderBy(priorityRank.asc(), qna.createAt.asc())
                .limit(limit)
                .fetch();
    }

    private OrderSpecifier<?>[] toOrderSpecifiers(Sort sort) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        if (!sort.isUnsorted()) {
            for (Sort.Order order : sort) {
                Order direction = order.isAscending() ? Order.ASC : Order.DESC;
                String prop = order.getProperty();
                if ("createdAt".equals(prop) || "createAt".equals(prop)) {
                    orders.add(new OrderSpecifier<>(direction, qna.createAt));
                } else if ("answeredAt".equals(prop)) {
                    orders.add(new OrderSpecifier<>(direction, qna.answeredAt).nullsLast());
                }
            }
        }

        if (orders.isEmpty()) {
            orders.add(new OrderSpecifier<>(Order.DESC, qna.createAt));
        }
        return orders.toArray(OrderSpecifier[]::new);
    }
}
