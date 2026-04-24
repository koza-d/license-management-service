package koza.licensemanagementservice.domain.audit.service;

import koza.licensemanagementservice.domain.audit.entity.AdminAuditLog;
import koza.licensemanagementservice.domain.audit.entity.EventCategory;
import koza.licensemanagementservice.domain.audit.repository.AdminAuditLogRepository;
import koza.licensemanagementservice.domain.license.entity.License;
import koza.licensemanagementservice.domain.license.log.dto.LicenseAdminStatusChangedEvent;
import koza.licensemanagementservice.domain.license.log.dto.LicenseExtendEvent;
import koza.licensemanagementservice.domain.license.repository.LicenseRepository;
import koza.licensemanagementservice.domain.member.entity.Member;
import koza.licensemanagementservice.domain.member.log.dto.MemberGradeChangedEvent;
import koza.licensemanagementservice.domain.member.log.dto.MemberRoleChangedEvent;
import koza.licensemanagementservice.domain.member.log.dto.MemberStatusChangedEvent;
import koza.licensemanagementservice.domain.member.repository.MemberRepository;
import koza.licensemanagementservice.domain.qna.log.dto.QnaAnswerUpdatedEvent;
import koza.licensemanagementservice.domain.qna.log.dto.QnaAnsweredEvent;
import koza.licensemanagementservice.domain.qna.log.dto.QnaPriorityChangedEvent;
import koza.licensemanagementservice.domain.session.log.dto.SessionBulkTerminatedEvent;
import koza.licensemanagementservice.domain.session.log.dto.SessionTerminatedEvent;
import koza.licensemanagementservice.domain.software.entity.Software;
import koza.licensemanagementservice.domain.software.log.dto.AdminSoftwareStatusChangedEvent;
import koza.licensemanagementservice.domain.software.repository.SoftwareRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminAuditLogListener {
    private static final String TARGET_LICENSE = "LICENSE";
    private static final String TARGET_MEMBER = "MEMBER";
    private static final String TARGET_SOFTWARE = "SOFTWARE";
    private static final String TARGET_SESSION = "SESSION";
    private static final String TARGET_QNA = "QNA";

    private final AdminAuditLogRepository auditLogRepository;
    private final MemberRepository memberRepository;
    private final LicenseRepository licenseRepository;
    private final SoftwareRepository softwareRepository;

    // ===== License =====

    @Async("auditLogExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onLicenseAdminStatusChanged(LicenseAdminStatusChangedEvent event) {
        if (event.getBeforeStatus() == event.getAfterStatus()) return;
        String actorEmail = resolveMemberEmail(event.getOperatorId());
        String label = resolveLicenseLabel(event.getTargetId());
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("before", event.getBeforeStatus().name());
        payload.put("after", event.getAfterStatus().name());
        payload.put("reason", event.getReason());
        save(EventCategory.LICENSE, "STATUS_CHANGED",
                event.getOperatorId(), actorEmail,
                TARGET_LICENSE, event.getTargetId(), label,
                String.format("라이센스 '%s' 상태 %s → %s",
                        label, event.getBeforeStatus(), event.getAfterStatus()),
                payload);
    }

    @Async("auditLogExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onLicenseExtended(LicenseExtendEvent event) {
        String actorEmail = resolveMemberEmail(event.getOperatorId());
        String label = resolveLicenseLabel(event.getLicenseId());
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("before", event.getBeforeExpiredAt());
        payload.put("after", event.getAfterExpiredAt());
        payload.put("periodMs", event.getPeriodMs());
        save(EventCategory.LICENSE, "EXTENDED",
                event.getOperatorId(), actorEmail,
                TARGET_LICENSE, event.getLicenseId(), label,
                String.format("라이센스 '%s' 만료일 연장", label),
                payload);
    }

    // ===== Member =====

    @Async("auditLogExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onMemberStatusChanged(MemberStatusChangedEvent event) {
        Member target = event.getTarget();
        Member operator = event.getOperator();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("before", event.getBefore().name());
        payload.put("after", event.getAfter().name());
        payload.put("reason", event.getReason());
        save(EventCategory.MEMBER, "STATUS_CHANGED",
                operator.getId(), operator.getEmail(),
                TARGET_MEMBER, target.getId(), target.getEmail(),
                String.format("회원 '%s' 상태 %s → %s",
                        target.getEmail(), event.getBefore(), event.getAfter()),
                payload);
    }

    @Async("auditLogExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onMemberGradeChanged(MemberGradeChangedEvent event) {
        Member target = event.getTarget();
        Member operator = event.getOperator();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("before", event.getBefore().name());
        payload.put("after", event.getAfter().name());
        payload.put("reason", event.getReason());
        save(EventCategory.MEMBER, "GRADE_CHANGED",
                operator.getId(), operator.getEmail(),
                TARGET_MEMBER, target.getId(), target.getEmail(),
                String.format("회원 '%s' 등급 %s → %s",
                        target.getEmail(), event.getBefore(), event.getAfter()),
                payload);
    }

    @Async("auditLogExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onMemberRoleChanged(MemberRoleChangedEvent event) {
        Member target = event.getTarget();
        Member operator = event.getOperator();
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("before", event.getBefore().name());
        payload.put("after", event.getAfter().name());
        payload.put("reason", event.getReason());
        save(EventCategory.MEMBER, "ROLE_CHANGED",
                operator.getId(), operator.getEmail(),
                TARGET_MEMBER, target.getId(), target.getEmail(),
                String.format("회원 '%s' 역할 %s → %s",
                        target.getEmail(), event.getBefore(), event.getAfter()),
                payload);
    }

    // ===== Software =====

    @Async("auditLogExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onSoftwareStatusChanged(AdminSoftwareStatusChangedEvent event) {
        if (event.getBeforeStatus() == event.getAfterStatus())
            return;

        String actorEmail = resolveMemberEmail(event.getOperatorId());
        String label = resolveSoftwareLabel(event.getTargetSoftwareId());

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("before", event.getBeforeStatus().name());
        payload.put("after", event.getAfterStatus().name());
        payload.put("reason", event.getReason());
        if (event.getUntil() != null)
            payload.put("until", event.getUntil());

        String summary = String.format("소프트웨어 '%s' 상태 %s → %s"
                , label, event.getBeforeStatus(), event.getAfterStatus());

        if (event.getUntil() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String format = event.getUntil().format(formatter);
            summary += String.format("(until  : %s)", format);
        }

        save(EventCategory.SOFTWARE, "STATUS_CHANGED",
                event.getOperatorId(), actorEmail,
                TARGET_SOFTWARE, event.getTargetSoftwareId(), label,
                summary,
                payload);
    }

    // ===== Session =====

    @Async("auditLogExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onSessionTerminated(SessionTerminatedEvent event) {
        String actorEmail = resolveMemberEmail(event.getOperatorId());
        String licenseLabel = resolveLicenseLabel(event.getLicenseId());
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sessionId", event.getSessionId());
        payload.put("licenseId", event.getLicenseId());
        payload.put("reason", event.getReason());
        save(EventCategory.SESSION, "TERMINATED",
                event.getOperatorId(), actorEmail,
                TARGET_SESSION, event.getLicenseId(), licenseLabel,
                String.format("세션 강제 종료 (license '%s')", licenseLabel),
                payload);
    }

    @Async("auditLogExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onSessionBulkTerminated(SessionBulkTerminatedEvent event) {
        String actorEmail = resolveMemberEmail(event.getOperatorId());
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sessionIds", event.getSessionIds());
        payload.put("terminated", event.getTerminated());
        payload.put("failed", event.getFailed());
        payload.put("reason", event.getReason());
        // 대량 종료는 특정 단일 target이 아니라 operator 본인을 target으로 기록
        save(EventCategory.SESSION, "BULK_TERMINATED",
                event.getOperatorId(), actorEmail,
                TARGET_MEMBER, event.getOperatorId(), actorEmail,
                String.format("세션 %d건 일괄 강제 종료 (실패 %d건)", event.getTerminated(), event.getFailed()),
                payload);
    }

    // ===== QnA =====

    @Async("auditLogExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onQnaAnswered(QnaAnsweredEvent event) {
        String actorEmail = resolveMemberEmail(event.getOperatorId());
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("askerId", event.getAskerId());
        payload.put("askerEmail", event.getAskerEmail());
        save(EventCategory.QNA, "ANSWERED",
                event.getOperatorId(), actorEmail,
                TARGET_QNA, event.getQnaId(), event.getQnaTitle(),
                String.format("문의 '%s' 답변 등록", event.getQnaTitle()),
                payload);
    }

    @Async("auditLogExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onQnaAnswerUpdated(QnaAnswerUpdatedEvent event) {
        String actorEmail = resolveMemberEmail(event.getOperatorId());
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("askerId", event.getAskerId());
        payload.put("askerEmail", event.getAskerEmail());
        payload.put("before", event.getBeforeAnswer());
        payload.put("after", event.getAfterAnswer());
        save(EventCategory.QNA, "ANSWER_UPDATED",
                event.getOperatorId(), actorEmail,
                TARGET_QNA, event.getQnaId(), event.getQnaTitle(),
                String.format("문의 '%s' 답변 수정", event.getQnaTitle()),
                payload);
    }

    @Async("auditLogExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onQnaPriorityChanged(QnaPriorityChangedEvent event) {
        String actorEmail = resolveMemberEmail(event.getOperatorId());
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("before", event.getBefore().name());
        payload.put("after", event.getAfter().name());
        save(EventCategory.QNA, "PRIORITY_CHANGED",
                event.getOperatorId(), actorEmail,
                TARGET_QNA, event.getQnaId(), event.getQnaTitle(),
                String.format("문의 '%s' 긴급도 %s → %s",
                        event.getQnaTitle(), event.getBefore(), event.getAfter()),
                payload);
    }

    // ===== helpers =====

    private void save(EventCategory category, String type,
                      Long actorId, String actorEmail,
                      String targetType, Long targetId, String targetLabel,
                      String summary, Map<String, Object> payload) {
        try {
            auditLogRepository.save(AdminAuditLog.builder()
                    .eventCategory(category)
                    .eventType(type)
                    .actorId(actorId)
                    .actorEmail(actorEmail)
                    .targetType(targetType)
                    .targetId(targetId)
                    .targetLabel(targetLabel)
                    .summary(summary)
                    .payload(payload)
                    .build());
        } catch (Exception e) {
            // 감사 로그 기록 실패가 주 플로우에 영향을 주면 안 됨 (AFTER_COMMIT이라 주 트랜잭션은 이미 커밋됨)
            log.error("감사 로그 저장 실패 category={}, type={}, targetId={}, reason={}",
                    category, type, targetId, e.getMessage(), e);
        }
    }

    private String resolveMemberEmail(Long id) {
        if (id == null) return null;
        return memberRepository.findById(id).map(Member::getEmail).orElse(null);
    }

    private String resolveLicenseLabel(Long id) {
        if (id == null) return null;
        return licenseRepository.findById(id)
                .map(License::getLicenseKey)
                .orElse("#" + id);
    }

    private String resolveSoftwareLabel(Long id) {
        if (id == null) return null;
        return softwareRepository.findById(id)
                .map(Software::getName)
                .orElse("#" + id);
    }
}
