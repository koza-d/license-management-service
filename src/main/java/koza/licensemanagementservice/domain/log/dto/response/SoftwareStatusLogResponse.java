package koza.licensemanagementservice.domain.log.dto.response;

import koza.licensemanagementservice.domain.log.entity.SoftwareStatusLog;
import koza.licensemanagementservice.domain.software.entity.SoftwareStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SoftwareStatusLogResponse {
    private Long id;
    private Long softwareId;
    private Long managerId;
    private String managerNickname;
    private SoftwareStatus previousStatus;
    private SoftwareStatus newStatus;
    private String reason;
    private LocalDateTime createdAt;

    public static SoftwareStatusLogResponse from(SoftwareStatusLog log) {
        return SoftwareStatusLogResponse.builder()
                .id(log.getId())
                .softwareId(log.getSoftware().getId())
                .managerId(log.getManager().getId())
                .managerNickname(log.getManager().getNickname())
                .previousStatus(log.getPreviousStatus())
                .newStatus(log.getNewStatus())
                .reason(log.getReason())
                .createdAt(log.getCreateAt())
                .build();
    }
}
