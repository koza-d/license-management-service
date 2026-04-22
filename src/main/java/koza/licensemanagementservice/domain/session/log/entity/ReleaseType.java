package koza.licensemanagementservice.domain.session.log.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReleaseType {
    NORMAL("정상적으로 해제"),
    TIMEOUT("타임아웃"),
    FORCE_CLOSE("관리자 강제 종료"),
    SYSTEM_ERROR("서버 오류");

    private final String desc;
}
