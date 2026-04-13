package koza.licensemanagementservice.domain.log.dto.response;

import koza.licensemanagementservice.domain.log.entity.MemberGradeLog;
import koza.licensemanagementservice.domain.member.entity.MemberGrade;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MemberGradeLogResponse {
    private Long id;
    private Long memberId;
    private Long managerId;
    private String managerNickname;
    private MemberGrade previousGrade;
    private MemberGrade newGrade;
    private String reason;
    private LocalDateTime createdAt;

    public static MemberGradeLogResponse from(MemberGradeLog log) {
        return MemberGradeLogResponse.builder()
                .id(log.getId())
                .memberId(log.getMember().getId())
                .managerId(log.getManager().getId())
                .managerNickname(log.getManager().getNickname())
                .previousGrade(log.getPreviousGrade())
                .newGrade(log.getNewGrade())
                .reason(log.getReason())
                .createdAt(log.getCreateAt())
                .build();
    }
}
