package koza.licensemanagementservice.domain.license.repository.condition;

import lombok.Data;

@Data
public class LicenseSearchCondition {
    private LicenseSearchTarget target; // 검색 대상 (라이센스명, 소프트웨어명 등)
    private String search; // 검색 키워드
    private Boolean hasActiveSession; // 활성화된 세션이 있는 라이센스 필터용
}
