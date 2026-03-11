package koza.licensemanagementservice.sessionLog.entity;

import jakarta.persistence.*;
import koza.licensemanagementservice.license.entity.License;
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
public class SessionLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "license_id")
    private License license;

    @Column(nullable = false)
    private String ipAddress;

    @Column(nullable = false)
    private String userAgent;

    @Column(nullable = false)
    private LocalDateTime verifyAt;

    @Column(nullable = false)
    private LocalDateTime releaseAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReleaseType releaseType;

}
