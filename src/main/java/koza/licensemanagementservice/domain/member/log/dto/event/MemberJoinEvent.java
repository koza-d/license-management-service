package koza.licensemanagementservice.domain.member.log.dto.event;

import lombok.Data;

import java.util.Map;

@Data
public class MemberJoinEvent {
    private final Long memberId;
    private final Map<String, Object> memberSnapshot;
}
