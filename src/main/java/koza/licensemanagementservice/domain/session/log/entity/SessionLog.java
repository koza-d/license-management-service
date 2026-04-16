package koza.licensemanagementservice.domain.session.log.entity;

import jakarta.persistence.*;
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "license_id")
    private License license;

    @Column(name = "session_id", length = 36, nullable = false)
    private String sessionId;

    @Column(name = "ip_address", length = 45, nullable = false)
    private String ipAddress;

    @Column(name = "user_agent", length = 500, nullable = false)
    private String userAgent;

    @Column(name = "verify_at", nullable = false)
    private LocalDateTime verifyAt;

    @Column(name = "release_at", nullable = false)
    private LocalDateTime releaseAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "release_type", length = 20, nullable = false)
    private ReleaseType releaseType;

}
