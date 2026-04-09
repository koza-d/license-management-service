package koza.licensemanagementservice.domain.qna.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import koza.licensemanagementservice.domain.qna.dto.response.QnaListResponse;
import koza.licensemanagementservice.domain.qna.entity.QnaStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static koza.licensemanagementservice.domain.qna.entity.QQnaQuestion.qnaQuestion;

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
}
