package koza.licensemanagementservice.software.entity;

import jakarta.persistence.*;
import koza.licensemanagementservice.global.common.BaseEntity;
import koza.licensemanagementservice.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "software")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class Software extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "name", length = 30, nullable = false)
    private String name;
    @Column(name = "version", length = 20, nullable = false)
    private String version;
    @Column(name = "api_key", length = 128, nullable = false)
    private String apiKey;

    @Column(name = "limit_license")
    private int limitLicense;

    public void updateInfo(String name, String version) {
        if (name != null) this.name = name;
        if (version != null) this.version = version;
    }
}
