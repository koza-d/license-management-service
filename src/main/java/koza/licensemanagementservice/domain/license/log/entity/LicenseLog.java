package koza.licensemanagementservice.domain.license.log.entity;

import jakarta.persistence.*;
import koza.licensemanagementservice.domain.license.entity.License;
import koza.licensemanagementservice.domain.member.entity.Member;
import koza.licensemanagementservice.global.common.LogBaseEntity;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "license_log")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class LicenseLog extends LogBaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "license_id", nullable = false)
    private License license;

    @ManyToOne(optional = false)
    @JoinColumn(name = "operator_id", nullable = false)
    private Member operator;

    @Enumerated(EnumType.STRING)
    @Column(name = "log_type", length = 20, nullable = false)
    private LicenseLogType logType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data", columnDefinition = "json", nullable = false)
    private Map<String, Object> data; // 변경된 데이터 { field: { before:data, after:data2 }, ... }

    @Column(name = "operated_at", nullable = false)
    private LocalDateTime operatedAt;
}
