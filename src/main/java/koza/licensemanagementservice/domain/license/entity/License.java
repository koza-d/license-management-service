package koza.licensemanagementservice.domain.license.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import koza.licensemanagementservice.global.common.BaseEntity;
import koza.licensemanagementservice.domain.software.entity.Software;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "licenses")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@DynamicUpdate
public class License extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "software_id")
    private Software software;

    @NotNull
    @Column(name = "name", length = 20)
    private String name;

    @NotNull
    @Column(name = "memo", length = 200)
    private String memo;

    @NotNull
    @Column(name = "license_key", length = 128)
    private String licenseKey;

    @NotNull
    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    @Column(name = "latest_active_at")
    private LocalDateTime latestActiveAt;

    @NotNull
    @Builder.Default
    @Getter(AccessLevel.NONE)
    @JdbcTypeCode(SqlTypes.JSON) // Map 을 DB JSON 컬럼에 매핑
    @Column(name = "local_variables", columnDefinition = "json")
    private Map<String, Object> localVariables = new HashMap<>(); // 변경된 지역변수만 담음

    @NotNull
    @Getter(AccessLevel.NONE)
    private boolean hasActiveSession;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private LicenseStatus status;

    public void updateName(String name) {
        this.name = name;
    }

    public void updateMemo(String memo) {
        this.memo = memo;
    }

    public void changeStatus(LicenseStatus status) {
        this.status = status;
    }

    /**
     * 소프트웨어의 default value인 localVariables와 라이센스의 localVariables를 합친 결과물을 반환합니다.
     */
    public Map<String, Object> getMergeLocalVariables() {
        // 지역변수 템플릿 + 실제 값 병합 로직
        Map<String, Object> defaultVars = software.getLocalVariables();
        Map<String, Object> modifiedVars = localVariables;

        Map<String, Object> finalVars = new HashMap<>(defaultVars);
        finalVars.putAll(modifiedVars);
        return finalVars;
    }

    /**
     * 정제되지 않은 localVariables를 반환합니다.
     * @see License#getMergeLocalVariables() (병합된 최종 localVariables)
     */
    public Map<String, Object> getRawLocalVariables() {
        return this.localVariables;
    }
    public void updateLocalVariables(Map<String, Object> localVariables) {
        this.localVariables.clear();
        if (localVariables != null)
            this.localVariables.putAll(localVariables);
    }

    public boolean hasActiveSession() {
        return this.hasActiveSession;
    }

    public void extendPeriod(int extendDays) {
        LocalDateTime now = LocalDateTime.now();
        if (expiredAt.isBefore(now))
            expiredAt = now;
        expiredAt = expiredAt.plusDays(extendDays);
    }

    public void verify() {
        this.hasActiveSession = true;
        this.latestActiveAt = LocalDateTime.now();
    }

    public void release() {
        this.hasActiveSession = false;
        this.latestActiveAt = LocalDateTime.now();
    }

    public Map<String, Object> toSnapshot() {
        Map<String, Object> localVariables = new HashMap<>(this.localVariables);
        return Map.of(
                "id", this.id,
                "name", this.name,
                "memo", this.memo,
                "licenseKey", this.licenseKey,
                "expiredAt", this.expiredAt,
                "status", this.status.name(),
                "localVariables", localVariables
        );
    }
}
