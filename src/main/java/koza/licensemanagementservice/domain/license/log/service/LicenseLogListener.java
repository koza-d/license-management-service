package koza.licensemanagementservice.domain.license.log.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import koza.licensemanagementservice.domain.license.entity.License;
import koza.licensemanagementservice.domain.license.log.dto.*;
import koza.licensemanagementservice.domain.license.log.entity.LicenseExtendLog;
import koza.licensemanagementservice.domain.license.log.entity.LicenseLog;
import koza.licensemanagementservice.domain.license.log.entity.LicenseLogType;
import koza.licensemanagementservice.domain.license.log.repository.LicenseExtendLogRepository;
import koza.licensemanagementservice.domain.license.log.repository.LicenseLogRepository;
import koza.licensemanagementservice.domain.license.repository.LicenseRepository;
import koza.licensemanagementservice.domain.member.entity.Member;
import koza.licensemanagementservice.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class LicenseLogListener {
    private final LicenseLogRepository logRepository;
    private final LicenseExtendLogRepository extendLogRepository;
    private final LicenseRepository licenseRepository;
    private final MemberRepository memberRepository;
    private final ObjectMapper objectMapper;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) // 커밋 성공 시에만 실행
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleLicenseBulkExtendEvent(LicenseBulkExtendEvent event) {
        Member operator = memberRepository.getReferenceById(event.getOperatorId());
        List<LicenseExtendLog> logs = new ArrayList<>();
        event.getLicenseIds().forEach(
                id -> {
                    License license = licenseRepository.getReferenceById(id);
                    LicenseExtendLog licenseExtendLog = LicenseExtendLog.builder()
                            .license(license)
                            .operator(operator)
                            .beforeExpiredAt(event.getBeforeExpiredAt().get(id))
                            .afterExpiredAt(event.getAfterExpiredAt().get(id))
                            .periodMs(event.getPeriodMs())
                            .build();
                    logs.add(licenseExtendLog);
                }
        );
        extendLogRepository.saveAll(logs);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) // 커밋 성공 시에만 실행
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleLicenseExtendEvent(LicenseExtendEvent event) {
        Member operator = memberRepository.getReferenceById(event.getOperatorId());
        License license = licenseRepository.getReferenceById(event.getLicenseId());
        LicenseExtendLog licenseExtendLog = LicenseExtendLog.builder()
                .license(license)
                .operator(operator)
                .beforeExpiredAt(event.getBeforeExpiredAt())
                .afterExpiredAt(event.getAfterExpiredAt())
                .periodMs(event.getPeriodMs())
                .build();
        extendLogRepository.save(licenseExtendLog);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleLicenseIssuedEvent(LicenseIssuedEvent event) {
        Member operator = memberRepository.getReferenceById(event.getOperatorId());
        License issuedLicense = licenseRepository.getReferenceById(event.getLicenseId());

        LicenseLog licenseLog = LicenseLog.builder()
                .license(issuedLicense)
                .operator(operator)
                .logType(LicenseLogType.ISSUED)
                .data(event.getSnapshot())
                .build();
        logRepository.save(licenseLog);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleLicenseModifiedEvent(LicenseModifiedEvent event) {
        License targetLicense = licenseRepository.getReferenceById(event.getTargetId());
        Member operator = memberRepository.getReferenceById(event.getOperatorId());

        try {
            Map<String, Object> diffValues = parseDiffValues(event.getBefore(), event.getAfter());
            if (diffValues.isEmpty())
                return;

            LicenseLog licenseLog = LicenseLog.builder()
                    .license(targetLicense)
                    .operator(operator)
                    .logType(LicenseLogType.MODIFIED)
                    .data(diffValues)
                    .build();
            logRepository.save(licenseLog);
        } catch (JsonProcessingException e) {
            log.error("LicenseId={} 해당 라이센스 변경 로그를 남기던 중 에러가 발생했습니다. 사유 : {}", event.getTargetId(), e.getMessage());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleLicenseStatusChangedEvent(LicenseStatusChangedEvent event) {
        License targetLicense = licenseRepository.getReferenceById(event.getTargetId());
        Member operator = memberRepository.getReferenceById(event.getOperatorId());

        if (event.getBeforeStatus() == event.getAfterStatus())
            return;
        Map<String, Object> diffValues = Map.of(
                "status", Map.of(
                        "before", event.getBeforeStatus(),
                        "after", event.getAfterStatus()
                ),
                "reason", event.getReason()
        );

        LicenseLog licenseLog = LicenseLog.builder()
                .license(targetLicense)
                .operator(operator)
                .logType(LicenseLogType.CHANGED_STATUS)
                .data(diffValues)
                .build();
        logRepository.save(licenseLog);
    }


    private Map<String, Object> parseDiffValues(Map<String, Object> before, Map<String, Object> after) throws JsonProcessingException {
        Map<String, Object> diff = new HashMap<>();

        for (String key : after.keySet()) {
            Object beforeValue = before.get(key);
            Object afterValue = after.get(key);

            if (beforeValue instanceof Map && afterValue instanceof Map) {
                beforeValue = objectMapper.writeValueAsString(beforeValue);
                afterValue = objectMapper.writeValueAsString(afterValue);
            }

            // 두 값이 다를 경우에만 추출
            if (!Objects.equals(beforeValue, afterValue)) {
                diff.put(key, Map.of(
                        "before", beforeValue == null ? "N/A" : beforeValue,
                        "after", afterValue == null ? "N/A" : afterValue
                ));
            }
        }
        return diff;
    }
}
