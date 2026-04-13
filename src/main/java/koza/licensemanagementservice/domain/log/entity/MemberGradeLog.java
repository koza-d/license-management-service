package koza.licensemanagementservice.domain.log.entity;

import jakarta.persistence.*;
import koza.licensemanagementservice.domain.member.entity.Member;
import koza.licensemanagementservice.domain.member.entity.MemberGrade;
import koza.licensemanagementservice.global.common.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member_grade_log")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class MemberGradeLog extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", nullable = false)
    private Member manager;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_grade", length = 20, nullable = false)
    private MemberGrade previousGrade;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_grade", length = 20, nullable = false)
    private MemberGrade newGrade;

    @Column(name = "reason", length = 500)
    private String reason;
}
