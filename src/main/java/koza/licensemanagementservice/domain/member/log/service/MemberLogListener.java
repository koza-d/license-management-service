package koza.licensemanagementservice.domain.member.log.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import koza.licensemanagementservice.domain.member.entity.Member;
import koza.licensemanagementservice.domain.member.log.dto.*;
import koza.licensemanagementservice.domain.member.log.entity.MemberLog;
import koza.licensemanagementservice.domain.member.log.entity.MemberLogType;
import koza.licensemanagementservice.domain.member.log.repository.MemberLogRepository;
import koza.licensemanagementservice.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberLogListener {
    private final MemberLogRepository memberLogRepository;
    private final MemberRepository memberRepository;
    private final ObjectMapper objectMapper;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleStatusChanged(MemberStatusChangedEvent event) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("before", event.getBefore().name());
        payload.put("after", event.getAfter().name());
        payload.put("reason", event.getReason());
        persist(event.getTarget(), event.getOperator(), MemberLogType.STATUS_CHANGE, payload);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleGradeChanged(MemberGradeChangedEvent event) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("before", event.getBefore().name());
        payload.put("after", event.getAfter().name());
        payload.put("reason", event.getReason());
        persist(event.getTarget(), event.getOperator(), MemberLogType.GRADE_CHANGE, payload);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleRoleChanged(MemberRoleChangedEvent event) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("before", event.getBefore().name());
        payload.put("after", event.getAfter().name());
        payload.put("reason", event.getReason());
        persist(event.getTarget(), event.getOperator(), MemberLogType.ROLE_CHANGE, payload);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleLoginSuccess(MemberLoginSuccessEvent event) {
        memberRepository.findById(event.getMemberId()).ifPresent(member -> {
            member.updateLastLoginAt();
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("joinType", event.getJoinType().name());
            payload.put("ipAddress", event.getIpAddress());
            payload.put("userAgent", event.getUserAgent());
            persist(member, member, MemberLogType.LOGIN_SUCCESS, payload);
        });
    }

    @Async
    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleLoginFail(MemberLoginFailEvent event) {
        memberRepository.findById(event.getMemberId()).ifPresent(member -> {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("joinType", event.getJoinType().name());
            payload.put("ipAddress", event.getIpAddress());
            payload.put("userAgent", event.getUserAgent());
            payload.put("failReason", event.getFailReason());
            persist(member, member, MemberLogType.LOGIN_FAIL, payload);
        });
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleWithdrawRequested(MemberWithdrawRequestedEvent event) {
        Member memberReference = memberRepository.getReferenceById(event.getMemberId());
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("reason", event.getReason());
        payload.put("scheduledAt", event.getScheduledAt());
        persist(memberReference, memberReference, MemberLogType.WITHDRAW_REQUESTED, payload);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleWithdrawCancelled(MemberWithdrawCancelledEvent event) {
        Member memberReference = memberRepository.getReferenceById(event.getMemberId());
        persist(memberReference, memberReference, MemberLogType.WITHDRAW_CANCELLED, new LinkedHashMap<>());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleWithdraw(MemberWithdrawEvent event) {
        Member memberReference = memberRepository.getReferenceById(event.getMemberId());
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("provider", event.getProvider());
        payload.put("finalGrade", event.getGrade());
        payload.put("reason", event.getReason());
        payload.put("registerAt", event.getRegisterAt());
        persist(memberReference, memberReference, MemberLogType.WITHDRAW, payload);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleJoin(MemberJoinEvent event) {
        Member memberReference = memberRepository.getReferenceById(event.getMemberId());
        persist(memberReference, memberReference, MemberLogType.JOIN, event.getMemberSnapshot());
    }

    private void persist(Member member, Member operator, MemberLogType type, Map<String, Object> payload) {
        try {
            String data = objectMapper.writeValueAsString(payload);
            MemberLog entity = MemberLog.builder()
                    .member(member)
                    .operator(operator)
                    .logType(type)
                    .data(data)
                    .build();
            memberLogRepository.save(entity);
        } catch (JsonProcessingException e) {
            log.error("MemberId={}, type={} 로그 저장 중 JSON 파싱 에러가 발생했습니다. 사유 : {}",
                    member.getId(), type, e.getMessage());
        }
    }
}
