package koza.licensemanagementservice.software.entity;

import jakarta.persistence.*;

@Entity
@Table(name="software_version")
public class SoftwareVersion {
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

    @Column(name = "download_url", length = 500)
    private String downloadURL;

    @Column(name = "memo", length = 200)
    private String memo;

}
