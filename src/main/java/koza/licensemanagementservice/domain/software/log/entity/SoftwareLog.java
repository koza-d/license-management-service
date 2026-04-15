package koza.licensemanagementservice.domain.software.log.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import koza.licensemanagementservice.domain.member.entity.Member;
import koza.licensemanagementservice.domain.software.entity.Software;
import koza.licensemanagementservice.global.common.LogBaseEntity;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Entity
@Table(name = "software_log")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@ToString
public class SoftwareLog extends LogBaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "software_id")
    private Software software;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id")
    private Member operator;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "log_type", length = 20)
    private SoftwareLogType logType;

    @NotNull
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data", columnDefinition = "json")
    private Map<String, Object> data; // 변경된 데이터 { field: { before:data, after:data2 }, ... }
}
