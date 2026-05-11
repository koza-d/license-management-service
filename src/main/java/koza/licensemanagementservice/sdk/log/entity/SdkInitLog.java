package koza.licensemanagementservice.sdk.log.entity;

import jakarta.persistence.*;
import koza.licensemanagementservice.domain.software.entity.Software;
import koza.licensemanagementservice.global.common.LogBaseEntity;
import lombok.*;

@Entity
@Table(name = "sdk_init_log")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class SdkInitLog extends LogBaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "is_success")
    private boolean isSuccess;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "software_id")
    private Software software;

    @Column(name = "app_id", length = 10, nullable = false)
    private String appId;

    @Column(name = "client_version", length = 50)
    private String clientVersion;

    @Column(name = "fail_code", length = 50)
    private String failCode;

    @Column(name = "ip_address", length = 45, nullable = false)
    private String ipAddress;

    @Column(name = "user_agent", length = 500, nullable = false)
    private String userAgent;
}

/*
CREATE TABLE `lms`.`sdk_init_log` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `is_success` TINYINT NULL,
  `software_id` BIGINT NULL,
  `app_id` VARCHAR(10) NOT NULL,
  `client_version` VARCHAR(50) NULL,
  `fail_code` VARCHAR(50) NULL,
  `ip_address` VARCHAR(45) NOT NULL,
  `user_agent` VARCHAR(500) NOT NULL,
  PRIMARY KEY (`id`));

 */