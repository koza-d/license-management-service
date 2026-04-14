package koza.licensemanagementservice.domain.software.log.entity;

import jakarta.persistence.*;
import koza.licensemanagementservice.domain.member.entity.Member;
import koza.licensemanagementservice.domain.software.entity.Software;
import koza.licensemanagementservice.domain.software.entity.SoftwareStatus;
import koza.licensemanagementservice.global.common.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "software_status_log")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class SoftwareStatusLog extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "software_id", nullable = false)
    private Software software;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", nullable = false)
    private Member manager;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 20, nullable = false)
    private SoftwareStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", length = 20, nullable = false)
    private SoftwareStatus newStatus;

    @Column(name = "reason", length = 500)
    private String reason;
}
