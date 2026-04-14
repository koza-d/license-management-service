package koza.licensemanagementservice.domain.member.log.dto.response;

import koza.licensemanagementservice.domain.member.entity.Member;
import koza.licensemanagementservice.domain.member.log.entity.MemberLog;
import koza.licensemanagementservice.domain.member.log.entity.MemberLogType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MemberLogResponse {
    private Long id;
    private Long memberId;
    private Long operatorId;
    private String operatorNickname;
    private MemberLogType logType;
    private String logTypeDesc;
    private String data;
    private LocalDateTime createdAt;

    public static MemberLogResponse from(MemberLog log) {
        Member op = log.getOperator();
        return MemberLogResponse.builder()
                .id(log.getId())
                .memberId(log.getMember().getId())
                .operatorId(op == null ? null : op.getId())
                .operatorNickname(op == null ? null : op.getNickname())
                .logType(log.getLogType())
                .logTypeDesc(log.getLogType().getDesc())
                .data(log.getData())
                .createdAt(log.getCreateAt())
                .build();
    }
}
