package koza.licensemanagementservice.software.entity;

import jakarta.persistence.*;
import koza.licensemanagementservice.global.common.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name="software_version")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SoftwareVersion extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "software_id", nullable = false)
    private Software software;

    @Column(name = "version", length = 50, nullable = false)
    private String version;

    @Column(name = "file_hash", length = 64)
    private String fileHash;

    @Column(name = "is_available")
    private boolean isAvailable;

    @Column(name = "download_url", length = 2048)
    private String downloadURL;

    @Column(name = "memo", length = 200)
    private String memo;

}
