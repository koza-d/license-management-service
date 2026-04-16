package koza.licensemanagementservice.domain.license.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
public class LicenseStat {
    private Long total; // 총 라이센스 수
    private Long expire; // 만료 라이센스 수
    private Long active; // 상태 'ACTIVE' 라이센스 수
    private Long banned; // 상태 'BANNED' 라이센스 수
    private Long activeSessions; // 사용중인 라이센스 수(세션 유효)
}
