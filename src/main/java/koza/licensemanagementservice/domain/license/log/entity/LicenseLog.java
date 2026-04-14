package koza.licensemanagementservice.domain.license.log.entity;

import jakarta.persistence.*;
import koza.licensemanagementservice.domain.license.entity.License;
import koza.licensemanagementservice.domain.member.entity.Member;
import koza.licensemanagementservice.global.common.LogBaseEntity;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

    @ManyToOne
    @JoinColumn(name = "license_id")
    private License license;

    @ManyToOne
    @JoinColumn(name = "operator_id")
    private Member operator;

    @Enumerated(EnumType.STRING)
    private LicenseLogType logType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data", columnDefinition = "json")
    private String data; // 변경된 데이터 { field: { before:data, after:data2 }, ... }
}
