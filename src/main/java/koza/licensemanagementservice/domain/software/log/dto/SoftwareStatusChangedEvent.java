package koza.licensemanagementservice.domain.software.log.dto;

import koza.licensemanagementservice.domain.member.entity.Member;
import koza.licensemanagementservice.domain.software.entity.Software;
import koza.licensemanagementservice.domain.software.entity.SoftwareStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SoftwareStatusChangedEvent {
    private Long targetSoftwareId;
    private Long operatorId;
    private SoftwareStatus beforeStatus;
    private SoftwareStatus afterStatus;
    private String reason;
}
