package koza.licensemanagementservice.license.entity;

import jakarta.persistence.*;
import koza.licensemanagementservice.global.common.BaseEntity;
import koza.licensemanagementservice.software.entity.Software;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "software_id")
    private Software software;

    @Column(name = "name", length = 20, nullable = false)
    private String name;
    @Column(name = "memo", length = 200)
    private String memo;
    @Column(name = "license_key", length = 128, nullable = false)
    private String licenseKey;
    @Column(name = "current_session_id", length = 32)
    private String currentSessionId;
    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;
    @Column(name = "latest_active_at")
    private LocalDateTime latestActiveAt;

    @JdbcTypeCode(SqlTypes.JSON) // Map 을 DB JSON 컬럼에 매핑
    @Column(name = "local_variables", columnDefinition = "json")
    private Map<String, Object> localVariables = new HashMap<>(); // 변경된 지역변수만 담음

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private LicenseStatus status;
    public void updateName(String name) {
        this.name = name;
    }

    public void updateMemo(String memo) {
        this.memo = memo;
    }

    public void updateLocalVariables(Map<String, Object> localVariables) {
        this.localVariables.clear();
        if (localVariables != null)
            this.localVariables.putAll(localVariables);
    }

    public void extendPeriod(int extendDays) {
        LocalDateTime now = LocalDateTime.now();
        if (expiredAt.isBefore(now))
            expiredAt = now;
        expiredAt = expiredAt.plusDays(extendDays);
    }

    public void verify(String sessionId) {
        this.currentSessionId = sessionId;
        this.latestActiveAt = LocalDateTime.now();
    }

    public void release() {
        this.latestActiveAt = LocalDateTime.now();
    }

}
