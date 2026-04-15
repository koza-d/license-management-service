package koza.licensemanagementservice.domain.license.log.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import koza.licensemanagementservice.domain.license.entity.License;
import koza.licensemanagementservice.domain.member.entity.Member;
import koza.licensemanagementservice.global.common.LogBaseEntity;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

    @NotNull
    @ManyToOne
    @JoinColumn(name = "license_id")
    private License license;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "operator_id")
    private Member operator;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "log_type", length = 20)
    private LicenseLogType logType;

    @NotNull
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data", columnDefinition = "json")
    private Map<String, Object> data; // 변경된 데이터 { field: { before:data, after:data2 }, ... }
}
