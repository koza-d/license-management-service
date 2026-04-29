package koza.licensemanagementservice.domain.software.log.dto.event;

import koza.licensemanagementservice.domain.member.entity.Member;
import koza.licensemanagementservice.domain.software.entity.Software;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class SoftwareModifiedEvent {
    private Long targetSoftwareId;
    private Long operatorId;
    private Map<String, Object> before;
    private Map<String, Object> after;
}
