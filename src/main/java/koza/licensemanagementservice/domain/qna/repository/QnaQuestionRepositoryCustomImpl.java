package koza.licensemanagementservice.domain.qna.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import koza.licensemanagementservice.domain.qna.dto.request.QnaAdminSearchCondition;
import koza.licensemanagementservice.domain.qna.dto.response.QnaAdminListResponse;
import koza.licensemanagementservice.domain.qna.dto.response.QnaListResponse;
import koza.licensemanagementservice.domain.qna.entity.QnaStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

import static koza.licensemanagementservice.domain.member.entity.QMember.member;
import static koza.licensemanagementservice.domain.qna.entity.QQnaQuestion.qnaQuestion;
import static koza.licensemanagementservice.domain.software.entity.QSoftware.software;

@RequiredArgsConstructor
public class QnaQuestionRepositoryCustomImpl implements QnaQuestionRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<QnaListResponse> findAllQuestions(String search, QnaStatus status, Pageable pageable) {
        return findQuestions(null, search, status, pageable);
    }

    @Override
    public Page<QnaListResponse> findBySoftwareId(Long softwareId, String search, QnaStatus status, Pageable pageable) {
        return findQuestions(softwareId, search, status, pageable);
    }

    private Page<QnaListResponse> findQuestions(Long softwareId, String search, QnaStatus status, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();

        if (softwareId != null) {
            builder.and(qnaQuestion.software.id.eq(softwareId));
        }
        if (status != null) {
            builder.and(qnaQuestion.status.eq(status));
        }
        if (search != null && !search.isBlank()) {
            builder.and(
                    qnaQuestion.software.name.containsIgnoreCase(search)
                            .or(qnaQuestion.title.containsIgnoreCase(search))
                            .or(qnaQuestion.content.containsIgnoreCase(search))
            );
        }

        List<QnaListResponse> content = queryFactory
                .select(Projections.constructor(QnaListResponse.class,
                        qnaQuestion.id,
                        qnaQuestion.software.name,
                        qnaQuestion.nickname,
                        qnaQuestion.title,
                        qnaQuestion.status,
                        qnaQuestion.createAt
                ))
                .from(qnaQuestion)
                .where(builder)
                .orderBy(qnaQuestion.createAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(qnaQuestion.count())
                .from(qnaQuestion)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    @Override
    public Page<QnaAdminListResponse> findByAdminCondition(QnaAdminSearchCondition condition, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();

        if (condition.getStatus() != null && !condition.getStatus().isEmpty()) {
            builder.and(qnaQuestion.status.in(condition.getStatus()));
        }

        if (condition.hasAnyFieldFilter()) {
            if (condition.getTitle() != null)
                builder.and(qnaQuestion.title.containsIgnoreCase(condition.getTitle()));
            if (condition.getAuthorEmail() != null)
                builder.and(member.email.containsIgnoreCase(condition.getAuthorEmail()));
            if (condition.getSoftwareName() != null)
                builder.and(software.name.containsIgnoreCase(condition.getSoftwareName()));
        } else if (condition.hasFullTextFilter()) {
            String q = condition.getQ();
            builder.and(
                    qnaQuestion.title.containsIgnoreCase(q)
                            .or(member.email.containsIgnoreCase(q))
                            .or(software.name.containsIgnoreCase(q))
            );
        }

        if (condition.getCreatedAfter() != null)
            builder.and(qnaQuestion.createAt.goe(condition.getCreatedAfter()));
        if (condition.getCreatedBefore() != null)
            builder.and(qnaQuestion.createAt.loe(condition.getCreatedBefore()));
        if (condition.getAnsweredAfter() != null)
            builder.and(qnaQuestion.answeredAt.goe(condition.getAnsweredAfter()));
        if (condition.getAnsweredBefore() != null)
            builder.and(qnaQuestion.answeredAt.loe(condition.getAnsweredBefore()));

        List<QnaAdminListResponse> content = queryFactory
                .select(Projections.constructor(QnaAdminListResponse.class,
                        qnaQuestion.id,
                        qnaQuestion.title,
                        member.email,
                        software.name,
                        qnaQuestion.status,
                        qnaQuestion.createAt,
                        qnaQuestion.answeredAt
                ))
                .from(qnaQuestion)
                .leftJoin(qnaQuestion.software, software)
                .leftJoin(qnaQuestion.member, member)
                .where(builder)
                .orderBy(toOrderSpecifiers(pageable.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(qnaQuestion.count())
                .from(qnaQuestion)
                .leftJoin(qnaQuestion.software, software)
                .leftJoin(qnaQuestion.member, member)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    private OrderSpecifier<?>[] toOrderSpecifiers(Sort sort) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        if (!sort.isUnsorted()) {
            for (Sort.Order order : sort) {
                Order direction = order.isAscending() ? Order.ASC : Order.DESC;
                String prop = order.getProperty();
                if ("createdAt".equals(prop) || "createAt".equals(prop)) {
                    orders.add(new OrderSpecifier<>(direction, qnaQuestion.createAt));
                } else if ("answeredAt".equals(prop)) {
                    orders.add(new OrderSpecifier<>(direction, qnaQuestion.answeredAt).nullsLast());
                }
            }
        }

        if (orders.isEmpty()) {
            orders.add(new OrderSpecifier<>(Order.DESC, qnaQuestion.createAt));
        }
        return orders.toArray(OrderSpecifier[]::new);
    }
}
