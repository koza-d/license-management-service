package koza.licensemanagementservice.domain.software.log.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SoftwareLogType {
    REGISTER("등록"),
    MODIFIED("수정"),
    CHANGE_VERSION("버전 변경"),
    ;
    private final String desc;
}
