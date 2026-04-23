package koza.licensemanagementservice.domain.member.log.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberLogType {
    STATUS_CHANGE("상태 변경"),
    GRADE_CHANGE("등급 변경"),
    ROLE_CHANGE("역할 변경"),
    LOGIN_SUCCESS("로그인 성공"),
    LOGIN_FAIL("로그인 실패"),
    WITHDRAW("회원탈퇴"),
    JOIN("회원가입")
    ;
    private final String desc;
}
