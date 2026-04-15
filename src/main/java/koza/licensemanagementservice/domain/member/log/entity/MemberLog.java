package koza.licensemanagementservice.domain.member.log.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import koza.licensemanagementservice.domain.member.entity.Member;
import koza.licensemanagementservice.global.common.LogBaseEntity;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "member_log")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@ToString
public class MemberLog extends LogBaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id")
    private Member operator;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "log_type", length = 30, nullable = false)
    private MemberLogType logType;

    @NotNull
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data", columnDefinition = "json")
    private String data;
}
