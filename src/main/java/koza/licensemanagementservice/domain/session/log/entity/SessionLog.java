package koza.licensemanagementservice.domain.session.log.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import koza.licensemanagementservice.domain.license.entity.License;
import koza.licensemanagementservice.global.common.LogBaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "session_log")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionLog extends LogBaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "license_id")
    private License license;

    @NotNull
    @Column(name = "session_id", length = 36)
    private String sessionId;

    @NotNull
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @NotNull
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @NotNull
    @Column(name = "verify_at")
    private LocalDateTime verifyAt;

    @NotNull
    @Column(name = "release_at")
    private LocalDateTime releaseAt;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "release_type", length = 20)
    private ReleaseType releaseType;

}
