package koza.licensemanagementservice.domain.audit.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import koza.licensemanagementservice.domain.audit.dto.request.AuditSearchCondition;
import koza.licensemanagementservice.domain.audit.dto.response.AuditLogResponse;
import koza.licensemanagementservice.audit.dto.response.QAuditLogResponse;
import koza.licensemanagementservice.audit.dto.response.QRecentAuditResponse;
import koza.licensemanagementservice.domain.audit.dto.response.RecentAuditResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static koza.licensemanagementservice.audit.entity.QAdminAuditLog.adminAuditLog;

@RequiredArgsConstructor
public class AdminAuditLogRepositoryCustomImpl implements AdminAuditLogRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<RecentAuditResponse> findRecent(int limit) {
        return queryFactory
                .select(new QRecentAuditResponse(
                        adminAuditLog.id,
                        adminAuditLog.eventCategory,
                        adminAuditLog.summary,
                        adminAuditLog.actorEmail,
                        adminAuditLog.createAt))
                .from(adminAuditLog)
                .orderBy(new OrderSpecifier<>(Order.DESC, adminAuditLog.createAt))
                .limit(limit)
                .fetch();
    }

    @Override
    public Page<AuditLogResponse> search(AuditSearchCondition condition, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();

        if (condition.getCategory() != null && !condition.getCategory().isEmpty()) {
            builder.and(adminAuditLog.eventCategory.in(condition.getCategory()));
        }

        if (condition.hasAnyFieldFilter()) {
            if (condition.getEventType() != null) {
                builder.and(adminAuditLog.eventType.eq(condition.getEventType()));
            }
            if (condition.getActorEmail() != null && !condition.getActorEmail().isBlank()) {
                builder.and(adminAuditLog.actorEmail.containsIgnoreCase(condition.getActorEmail()));
            }
            if (condition.getTargetType() != null && condition.getTargetId() != null) {
                builder.and(adminAuditLog.targetType.eq(condition.getTargetType()));
                builder.and(adminAuditLog.targetId.eq(condition.getTargetId()));
            }
        } else if (condition.hasFullTextFilter()) {
            String q = condition.getQ();
            builder.and(
                    adminAuditLog.summary.containsIgnoreCase(q)
                            .or(adminAuditLog.targetLabel.containsIgnoreCase(q))
                            .or(adminAuditLog.actorEmail.containsIgnoreCase(q))
            );
        }

        if (condition.getFrom() != null) {
            builder.and(adminAuditLog.createAt.goe(condition.getFrom()));
        }
        if (condition.getTo() != null) {
            builder.and(adminAuditLog.createAt.loe(condition.getTo()));
        }

        List<AuditLogResponse> content = queryFactory
                .select(new QAuditLogResponse(
                        adminAuditLog.id,
                        adminAuditLog.eventCategory,
                        adminAuditLog.eventType,
                        adminAuditLog.actorEmail,
                        adminAuditLog.targetType,
                        adminAuditLog.targetId,
                        adminAuditLog.targetLabel,
                        adminAuditLog.summary,
                        adminAuditLog.createAt))
                .from(adminAuditLog)
                .where(builder)
                .orderBy(new OrderSpecifier<>(Order.DESC, adminAuditLog.createAt))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(adminAuditLog.count())
                .from(adminAuditLog)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }
}
