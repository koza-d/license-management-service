package koza.licensemanagementservice.domain.member.log.entity;

import jakarta.persistence.*;
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "operator_id")
    private Member operator;

    @Enumerated(EnumType.STRING)
    @Column(name = "log_type", length = 30, nullable = false)
    private MemberLogType logType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data", columnDefinition = "json", nullable = false)
    private String data;
}
