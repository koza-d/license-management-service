package koza.licensemanagementservice.domain.software.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import koza.licensemanagementservice.domain.software.entity.Software;
import koza.licensemanagementservice.domain.software.entity.SoftwareStatus;
import koza.licensemanagementservice.domain.software.version.entity.SoftwareVersion;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Getter
@Builder
public class SoftwareAdminDetailResponse {
    private Long id;
    private String ownerEmail;
    private String ownerNickname;
    private String softwareName;
    private String latestVersion;
    private SoftwareStatus softwareStatus;
    private String appId;
    private int licenseCount; // 보유 라이센스 수
    private int limitLicense; // 보유 제한 라이센스 수
    private Map<String, Object> globalVariables;
    private Map<String, Object> localVariables;
    private LocalDateTime createAt;

    @QueryProjection
    public SoftwareAdminDetailResponse(Long id, String ownerEmail, String ownerNickname, String softwareName, String latestVersion, SoftwareStatus softwareStatus, String appId, int licenseCount, int limitLicense, Map<String, Object> globalVariables, Map<String, Object> localVariables, LocalDateTime createAt) {
        this.id = id;
        this.ownerEmail = ownerEmail;
        this.ownerNickname = ownerNickname;
        this.softwareName = softwareName;
        this.latestVersion = latestVersion;
        this.softwareStatus = softwareStatus;
        this.appId = appId;
        this.licenseCount = licenseCount;
        this.limitLicense = limitLicense;
        this.globalVariables = globalVariables;
        this.localVariables = localVariables;
        this.createAt = createAt;
    }
}
