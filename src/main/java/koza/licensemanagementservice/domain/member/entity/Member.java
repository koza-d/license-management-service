package koza.licensemanagementservice.domain.member.entity;

import jakarta.persistence.*;
import koza.licensemanagementservice.global.common.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Entity
@Table(name="members")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class Member extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", unique = true, length = 50)
    private String email;

    @Column(name = "password", length = 200)
    private String password;

    @Column(name = "nickname", length = 20, nullable = false)
    private String nickname;

    @Column(name = "profile_url")
    private String profileURL;

    @Column(name = "provider", length = 50)
    private String provider; // 소셜 브랜드명

    @Column(name = "provider_id", length = 255)
    private String providerId; // 소셜이 부여한 고유 ID

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "grade", length = 20, nullable = false)
    private MemberGrade grade = MemberGrade.BASIC;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private MemberStatus status = MemberStatus.ACTIVE;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "member_roles", joinColumns = @JoinColumn(name = "member_id"))
    @Builder.Default
    private List<String> roles = new ArrayList<>();

    public void changeProfileURL(String profileURL) {
        this.profileURL = profileURL;
    }

    public void changeGrade(MemberGrade grade) {
        this.grade = grade;
    }

    public void changeStatus(MemberStatus status) {
        this.status = status;
    }

    public void changeRole(MemberRole role) {
        this.roles = new ArrayList<>(List.of(role.getAuthority()));
    }

    public MemberRole getRole() {
        return MemberRole.from(this.roles);
    }

    public void updateLastLoginAt() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public void withdraw() {
        this.email = "#탈퇴한유저";
        this.password = null;
        this.status = MemberStatus.WITHDRAW;
    }

    public Map<String, Object> toSnapshot() {
        return Map.of(
                "nickname", this.nickname,
                "provider", this.provider
        );

    }
}
