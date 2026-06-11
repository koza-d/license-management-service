package koza.licensemanagementservice.domain.license.dto.condition;

import koza.licensemanagementservice.domain.license.entity.LicenseStatus;
import lombok.Data;

@Data
public class LicenseSearchCondition {
    private LicenseSearchTarget target; // 검색 대상 (라이센스명, 메모 등)
    private Long softwareId; // null 이면 전체
    private LicenseStatus status; // 라이센스 상태 필터
    private String search; // 검색 키워드
    private Boolean hasActiveSession; // 활성화된 세션이 있는 라이센스 필터용
}
