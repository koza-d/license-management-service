package koza.licensemanagementservice.domain.license.entity;

import jakarta.persistence.*;
import koza.licensemanagementservice.global.common.BaseEntity;
import koza.licensemanagementservice.domain.software.entity.Software;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "software_id", nullable = false)
    private Software software;

    @Column(name = "name", length = 20, nullable = false)
    private String name;

    @Column(name = "memo", length = 200, nullable = false)
    private String memo;

    @Column(name = "license_key", length = 128, nullable = false)
    private String licenseKey;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    @Column(name = "latest_active_at")
    private LocalDateTime latestActiveAt;

    @Builder.Default
    @Getter(AccessLevel.NONE)
    @JdbcTypeCode(SqlTypes.JSON) // Map 을 DB JSON 컬럼에 매핑
    @Column(name = "local_variables", columnDefinition = "json", nullable = false)
    private Map<String, String> localVariables = new HashMap<>(); // 변경된 지역변수만 담음

    @Getter(AccessLevel.NONE)
    private boolean hasActiveSession;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private LicenseStatus status;

    @Column(name = "status_until")
    private LocalDateTime statusUntil;

    @Column(name = "status_reason", length = 100)
    private String statusReason;

    @Column(name = "start_duration_days")
    private int startDurationDays; // 최초 부여 기간

    @Column(name = "started_at")
    private LocalDateTime startedAt; // 최초 사용일

    public void updateName(String name) {
        this.name = name;
    }

    public void updateMemo(String memo) {
        this.memo = memo;
    }

    public void changeStatus(LicenseStatus status) {
        changeStatus(status, null, null);
    }

    public void changeStatus(LicenseStatus status, LocalDateTime until, String reason) {
        this.status = status;
        this.statusUntil = until;
        this.statusReason = reason;
    }

    /**
     * 소프트웨어의 default value인 localVariables와 라이센스의 localVariables를 합친 결과물을 반환합니다.
     */
    public Map<String, String> getMergeLocalVariables() {
        // 지역변수 템플릿 + 실제 값 병합 로직
        Map<String, String> defaultVars = software.getLocalVariables();
        Map<String, String> modifiedVars = localVariables;

        Map<String, String> finalVars = new HashMap<>(defaultVars);
        finalVars.putAll(modifiedVars);
        return finalVars;
    }

    /**
     * 정제되지 않은 localVariables를 반환합니다.
     *
     * @see License#getMergeLocalVariables() (병합된 최종 localVariables)
     */
    public Map<String, String> getRawLocalVariables() {
        return this.localVariables;
    }

    public void updateLocalVariables(Map<String, String> localVariables) {
        this.localVariables.clear();
        if (localVariables != null)
            this.localVariables.putAll(localVariables);
    }

    public boolean hasActiveSession() {
        return this.hasActiveSession;
    }

    public void extendPeriod(int extendDays) {
        if (this.status == LicenseStatus.INACTIVE)
            throw new BusinessException(ErrorCode.LICENSE_NOT_ACTIVATED);

        LocalDateTime now = LocalDateTime.now();
        if (expiredAt.isBefore(now))
            expiredAt = now;
        expiredAt = expiredAt.plusDays(extendDays);
    }

    public void verify() {
        this.hasActiveSession = true;
        this.latestActiveAt = LocalDateTime.now();

        // 최초 인증 시 활성상태로 변경
        if (this.status == LicenseStatus.INACTIVE)
            this.startActive();
    }

    public void startActive() {
        if (this.status != LicenseStatus.INACTIVE)
            throw new BusinessException(ErrorCode.LICENSE_NOT_ACTIVATED);

        this.startedAt = LocalDateTime.now();
        this.expiredAt = LocalDate.now().plusDays(startDurationDays + 1).atStartOfDay();
        this.status = LicenseStatus.ACTIVE;
    }

    public void release(Map<String, String> changedLocalVariables) {
        this.hasActiveSession = false;
        this.latestActiveAt = LocalDateTime.now();
        if (changedLocalVariables != null)
            this.getRawLocalVariables().putAll(changedLocalVariables);
    }

    public Map<String, Object> toSnapshot() {
        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("id", this.id);
        snapshot.put("softwareId", this.software != null ? this.software.getId() : null);
        snapshot.put("name", this.name);
        snapshot.put("memo", this.memo);
        snapshot.put("licenseKey", this.licenseKey);
        snapshot.put("expiredAt", this.expiredAt);
        snapshot.put("latestActiveAt", this.latestActiveAt);
        snapshot.put("localVariables", new HashMap<>(this.localVariables));
        snapshot.put("hasActiveSession", this.hasActiveSession);
        snapshot.put("status", this.status.name());
        snapshot.put("statusUntil", this.statusUntil);
        snapshot.put("statusReason", this.statusReason);
        snapshot.put("startDurationDays", this.startDurationDays);
        snapshot.put("startedAt", this.startedAt);
        return snapshot;
    }
}
