package koza.licensemanagementservice.verification.log.entity;

import jakarta.persistence.*;
import koza.licensemanagementservice.domain.license.entity.License;
import koza.licensemanagementservice.domain.software.entity.Software;
import koza.licensemanagementservice.global.common.LogBaseEntity;
import lombok.*;


@Entity
@Table(name = "verify_log")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@ToString
public class VerifyLog extends LogBaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "is_success")
    private boolean isSuccess;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "software_id")
    private Software software;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "license_id")
    private License license;

    @Column(name = "app_id", length = 10, nullable = false)
    private String appId;

    @Column(name = "license_key", length = 128, nullable = false)
    private String licenseKey;

    @Column(name = "fail_code", length = 50)
    private String failCode;

    @Column(name = "ip_address", length = 45, nullable = false)
    private String ipAddress;

    @Column(name = "user_agent", length = 500, nullable = false)
    private String userAgent;
}
