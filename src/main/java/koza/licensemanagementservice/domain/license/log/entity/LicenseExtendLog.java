package koza.licensemanagementservice.domain.license.log.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import koza.licensemanagementservice.domain.license.entity.License;
import koza.licensemanagementservice.domain.member.entity.Member;
import koza.licensemanagementservice.global.common.LogBaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "license_extend_log")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class LicenseExtendLog extends LogBaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "license_id")
    private License license;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "operator_id")
    private Member operator;

    @NotNull
    @Column(name = "before_expired_at")
    private LocalDateTime beforeExpiredAt;

    @NotNull
    @Column(name = "after_expired_at")
    private LocalDateTime afterExpiredAt;

    @NotNull
    @Column(name = "period_ms")
    private Long periodMs;
}
