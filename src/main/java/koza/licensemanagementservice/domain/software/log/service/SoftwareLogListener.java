package koza.licensemanagementservice.domain.software.log.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import koza.licensemanagementservice.domain.member.entity.Member;
import koza.licensemanagementservice.domain.software.entity.Software;
import koza.licensemanagementservice.domain.software.log.dto.SoftwareCreatedEvent;
import koza.licensemanagementservice.domain.software.log.dto.SoftwareModifiedEvent;
import koza.licensemanagementservice.domain.software.log.dto.SoftwareVersionChangedEvent;
import koza.licensemanagementservice.domain.software.log.entity.SoftwareLog;
import koza.licensemanagementservice.domain.software.log.entity.SoftwareLogType;
import koza.licensemanagementservice.domain.software.log.repository.SoftwareLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class SoftwareLogListener {
    private final SoftwareLogRepository softwareLogRepository;
    private final ObjectMapper objectMapper;

    @Async // 비동기로 실행 (메인 스레드와 분리)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) // 커밋 성공 시에만 실행
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleSoftwareCreatedEvent(SoftwareCreatedEvent event) {
        Software createdSoftware = event.getCreatedSoftware();
        Member operator = event.getOperator();
        try {
            String value = objectMapper.writeValueAsString(createdSoftware.toSnapshot());
            SoftwareLog softwareLog = SoftwareLog.builder()
                    .software(createdSoftware)
                    .operator(operator)
                    .logType(SoftwareLogType.REGISTER)
                    .data(value)
                    .build();

            softwareLogRepository.save(softwareLog);
        } catch (JsonProcessingException e) {
            log.error("SoftwareId={} 을 저장 중 JSON 파싱 에러가 발생해 소프트웨어 생성 로그 저장에 실패했습니다. 사유 : {}", event.getCreatedSoftware().getId(), e.getMessage());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) // 커밋 성공 시에만 실행
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleSoftwareModifiedEvent(SoftwareModifiedEvent event) {
        Software targetSoftware = event.getTargetSoftware();
        Member operator = event.getOperator();
        try {
            Map<String, Object> diffValues = parseDiffValues(event.getBefore(), event.getAfter());
            if (diffValues.isEmpty())
                return;

            String value = objectMapper.writeValueAsString(diffValues);
            SoftwareLog softwareLog = SoftwareLog.builder()
                    .software(targetSoftware)
                    .operator(operator)
                    .logType(SoftwareLogType.MODIFIED)
                    .data(value)
                    .build();
            softwareLogRepository.save(softwareLog);
        } catch (JsonProcessingException e) {
            log.error("SoftwareId={} 의 변경사항을 저장중 JSON 파싱 에러가 발생해 로그 저장에 실패했습니다. 사유 : {}", event.getTargetSoftware().getId(), e.getMessage());
        }
    }


    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) // 커밋 성공 시에만 실행
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleSoftwareVersionChangedEvent(SoftwareVersionChangedEvent event) {
        Software targetSoftware = event.getTargetSoftware();
        Member operator = event.getOperator();
        try {
            Map<String, String> data = Map.of(
                    "before", objectMapper.writeValueAsString(event.getBeforeVersion()),
                    "after", objectMapper.writeValueAsString(event.getAfterVersion())
            );
            if (data.get("after").equals(data.get("before")))
                return;

            String value = objectMapper.writeValueAsString(data);
            SoftwareLog softwareLog = SoftwareLog.builder()
                    .software(targetSoftware)
                    .operator(operator)
                    .logType(SoftwareLogType.CHANGE_VERSION)
                    .data(value)
                    .build();
            softwareLogRepository.save(softwareLog);
        } catch (JsonProcessingException e) {
            log.error("SoftwareId={} 버전 변경 중 JSON 파싱 에러가 발생해 로그 저장에 실패했습니다. 사유 : {}", event.getTargetSoftware().getId(), e.getMessage());
        }

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
