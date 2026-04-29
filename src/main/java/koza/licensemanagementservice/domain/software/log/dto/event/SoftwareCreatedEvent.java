package koza.licensemanagementservice.domain.software.log.dto.event;

import koza.licensemanagementservice.domain.member.entity.Member;
import koza.licensemanagementservice.domain.software.entity.Software;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Builder
@Getter
@AllArgsConstructor
public class SoftwareCreatedEvent {
    private Long targetSoftwareId;
    private Long operatorId;
    private Map<String, Object> softwareSnapshot;
}
