package koza.licensemanagementservice.software.entity;

import jakarta.persistence.*;
import koza.licensemanagementservice.global.common.BaseEntity;
import koza.licensemanagementservice.software.dto.SoftwareVersionDTO;
import lombok.*;

@Entity
@Getter
@Table(name="software_version")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SoftwareVersion extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
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

    public void updateVersionInfo(SoftwareVersionDTO.UpdateRequest request) {
        if (request.getVersion() != null) this.version = request.getVersion();
        if (request.getFileHash() != null) this.fileHash = request.getFileHash();
        if (request.getAvailable() != null) this.isAvailable = request.getAvailable();
        if (request.getDownloadURL() != null) this.downloadURL = request.getDownloadURL();
        if (request.getMemo() != null) this.memo = request.getMemo();
    }
}
