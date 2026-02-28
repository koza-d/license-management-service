package koza.licensemanagementservice.global.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    INVALID_INPUT_VALUE(400, "COMMON_001", "입력값이 올바르지 않습니다."),
    NOT_FOUND(404, "COMMON_002", "찾을 수 없는 리소스입니다."),
    ACCESS_DENIED(403, "COMMON_003", "접근 권한이 없습니다."),
    UNAUTHORIZED(401, "COMMON_004", "로그인이 필요합니다."),
    METADATA_FORMAT_WRONG(413, "COMMON_005", "메타데이터 형식이 잘못됐습니다."),


    DUPLICATE_EMAIL(409, "MEMBER_001", "이미 가입된 이메일입니다."),
    INCORRECT_EMAIL_OR_PASSWORD(401, "MEMBER_002", "이메일 또는 비밀번호가 유효하지 않습니다."),

    NO_RESULT_LICENSE(404, "LICENSE_001", "라이센스 검색 결과가 없습니다."),

    INTERNAL_SERVER_ERROR(500, "SERVER_001", "서버 내부 오류가 발생했습니다.");

    private final int status;
    private final String code;
    private final String message;
}
