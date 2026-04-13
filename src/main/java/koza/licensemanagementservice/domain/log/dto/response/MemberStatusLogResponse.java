package koza.licensemanagementservice.domain.log.dto.response;

import koza.licensemanagementservice.domain.log.entity.MemberStatusLog;
import koza.licensemanagementservice.domain.member.entity.MemberStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MemberStatusLogResponse {
    private Long id;
    private Long memberId;
    private Long managerId;
    private String managerNickname;
    private MemberStatus action;
    private String reason;
    private LocalDateTime createdAt;

    public static MemberStatusLogResponse from(MemberStatusLog log) {
        return MemberStatusLogResponse.builder()
                .id(log.getId())
                .memberId(log.getMember().getId())
                .managerId(log.getManager().getId())
                .managerNickname(log.getManager().getNickname())
                .action(log.getAction())
                .reason(log.getReason())
                .createdAt(log.getCreateAt())
                .build();
    }
}
