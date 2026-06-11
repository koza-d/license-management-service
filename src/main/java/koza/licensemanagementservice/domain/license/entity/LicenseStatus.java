package koza.licensemanagementservice.domain.license.entity;

public enum LicenseStatus {
    INACTIVE, // 첫 발급 상태. 한 번도 사용하지 않은 상태(사용가능)
    ACTIVE, // 발급 후 실사용한 상태 (사용가능)
    BANNED, // 정지된 상태 (사용불가)
    EXPIRED // 만료된 상태 (사용불가)
}
