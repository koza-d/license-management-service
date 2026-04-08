package koza.licensemanagementservice.domain.member.entity;

import jakarta.persistence.*;
import koza.licensemanagementservice.global.common.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

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

    @Column(unique = true, length = 50)
    private String email;

    @Column(length = 200)
    private String password;

    @Column(length = 20, nullable = false)
    private String nickname;

    @Column(name = "profile_url")
    private String profileURL;

    private String provider; // 소셜 브랜드명
    private String providerId; // 소셜이 부여한 고유 ID

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "member_roles", joinColumns = @JoinColumn(name = "member_id"))
    @Builder.Default
    private List<String> roles = new ArrayList<>();


    public void changeProfileURL(String profileURL) {
        this.profileURL = profileURL;
    }
}
