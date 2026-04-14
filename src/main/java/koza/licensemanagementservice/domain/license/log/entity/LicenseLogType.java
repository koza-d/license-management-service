package koza.licensemanagementservice.domain.license.log.entity;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LicenseLogType {
    ISSUED("발급"),
    MODIFIED("수정")
    ;

    private final String desc;
}
