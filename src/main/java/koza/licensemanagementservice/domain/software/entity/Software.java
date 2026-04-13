package koza.licensemanagementservice.domain.software.entity;

import jakarta.persistence.*;
import koza.licensemanagementservice.domain.member.entity.Member;
import koza.licensemanagementservice.domain.software.version.entity.SoftwareVersion;
import koza.licensemanagementservice.global.common.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "software")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class Software extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "software", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SoftwareVersion> versions;

    @Column(name = "name", length = 30, nullable = false)
    private String name;

    @Column(name = "api_key", length = 128, nullable = false)
    private String apiKey;

    @Builder.Default
    @JdbcTypeCode(SqlTypes.JSON) // Map 을 DB JSON 컬럼에 매핑
    @Column(name = "global_variables", columnDefinition = "json")
    private Map<String, Object> globalVariables = new HashMap<>(); // 라이센스마다 똑같이 적용될 전역 변수

    @Builder.Default
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "local_variables", columnDefinition = "json")
    private Map<String, Object> localVariables = new HashMap<>(); // 라이센스 별로 따로 설정가능한 변수

    @Column(name = "limit_license")
    private int limitLicense;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private SoftwareStatus status = SoftwareStatus.ACTIVE;
  
    public void changeLatestVersion(String latestVersion, List<SoftwareVersion> versions) {
        versions.forEach(v -> v.setLatest(v.getVersion().equals(latestVersion)));
    }

    public void addVersion(SoftwareVersion version) {
        if (this.versions == null)
            this.versions = new ArrayList<>();
        this.versions.add(version);
        version.setSoftware(this);
    }

    public void updateGlobalVariables(Map<String, Object> globalVariables) {
        this.globalVariables.clear();
        if (globalVariables != null)
            this.globalVariables.putAll(globalVariables);
    }

    public void updateLocalVariables(Map<String, Object> localVariables) {
        this.localVariables.clear();
        if (localVariables != null)
            this.localVariables.putAll(localVariables);
    }

    public void updateInfo(String name) {
        if (name != null) this.name = name;
    }

    public Map<String, Object> toSnapshot() {
        Map<String, Object> globalVariables = new HashMap<>(this.globalVariables);
        Map<String, Object> localVariables = new HashMap<>(this.localVariables);
        return Map.of(
                "id", id,
                "memberId", member.getId(),
                "versions", versions.stream().map(SoftwareVersion::getId).toList(),
                "name", name,
                "apiKey", apiKey,
                "globalVariables", globalVariables,
                "localVariables", localVariables,
                "limitLicense", limitLicense
        );
    }

    public void changeStatus(SoftwareStatus status) {
        this.status = status;
    }
}
