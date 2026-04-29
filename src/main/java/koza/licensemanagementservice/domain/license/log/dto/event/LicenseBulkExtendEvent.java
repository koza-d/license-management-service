package koza.licensemanagementservice.domain.license.log.dto.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class LicenseBulkExtendEvent {
    private Long operatorId;
    private List<Long> licenseIds;
    private Map<Long, LocalDateTime> beforeExpiredAt;
    private Map<Long, LocalDateTime> afterExpiredAt;
    private Long periodMs;
}
