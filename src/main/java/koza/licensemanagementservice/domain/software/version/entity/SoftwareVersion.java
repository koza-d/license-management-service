package koza.licensemanagementservice.domain.software.version.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import koza.licensemanagementservice.global.common.BaseEntity;
import koza.licensemanagementservice.domain.software.entity.Software;
import koza.licensemanagementservice.domain.software.version.dto.SoftwareVersionUpdateRequest;
import lombok.*;

import java.util.Map;

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
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "software_id")
    private Software software;

    @NotNull
    @Column(name = "version", length = 20)
    private String version;

    @Column(name = "file_hash", length = 64)
    private String fileHash;

    @NotNull
    @Column(name = "is_latest")
    private boolean isLatest;

    @NotNull
    @Column(name = "is_available")
    private boolean isAvailable;

    @Column(name = "download_url", length = 2083)
    private String downloadURL;

    @Column(name = "memo", length = 200)
    private String memo;

    public void updateVersionInfo(SoftwareVersionUpdateRequest request) {
        if (request.getVersion() != null) this.version = request.getVersion();
        if (request.getFileHash() != null) this.fileHash = request.getFileHash();
        if (request.getAvailable() != null) this.isAvailable = request.getAvailable();
        if (request.getDownloadURL() != null) this.downloadURL = request.getDownloadURL();
        if (request.getMemo() != null) this.memo = request.getMemo();
    }

    public void setLatest(boolean isLatest) {
        this.isLatest = isLatest;
    }

    public Map<String, Object> toSnapshot() {
        return Map.of(
                "id", id,
                "version", version
        );
    }
}
