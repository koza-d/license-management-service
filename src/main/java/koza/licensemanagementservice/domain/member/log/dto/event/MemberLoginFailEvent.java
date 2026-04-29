package koza.licensemanagementservice.domain.member.log.dto.event;

import koza.licensemanagementservice.auth.dto.SocialProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class MemberLoginFailEvent {
    private Long memberId;
    private String provider;
    private String ipAddress;
    private String userAgent;
    private String failReason;
}
