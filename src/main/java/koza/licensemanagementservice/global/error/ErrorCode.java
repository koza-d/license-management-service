package koza.licensemanagementservice.global.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // 공통 예외
    INVALID_INPUT_VALUE(400, "COMMON_001", "입력값이 올바르지 않습니다."),
    NOT_FOUND(404, "COMMON_002", "찾을 수 없는 리소스입니다."),
    ACCESS_DENIED(403, "COMMON_003", "접근 권한이 없습니다."),
    UNAUTHORIZED(401, "COMMON_004", "로그인이 필요합니다."),
    METADATA_FORMAT_WRONG(413, "COMMON_005", "메타데이터 형식이 잘못됐습니다."),
        
    // 회원 도메인 관련 예외
    DUPLICATE_EMAIL(409, "MEMBER_001", "이미 가입된 이메일입니다."),
    INCORRECT_EMAIL_OR_PASSWORD(401, "MEMBER_002", "이메일 또는 비밀번호가 유효하지 않습니다."),

    // 소프트웨어 버전 관련 예외
    DUPLICATE_VERSION(409, "VERSION_001", "이미 등록돼있는 버전입니다."),

    // 라이센스 인증 로직 예외
    NOT_FOUND_LICENSE(404, "VERIFICATION_001", "존재하지 않는 라이센스 입니다."),
    EXPIRED_LICENSE(403, "VERIFICATION_002", "만료된 라이센스 입니다."),
    ALREADY_USE_LICENSE(409, "VERIFICATION_003", "이미 사용 중인 라이센스 입니다."),

    // 세션 로직 예외
    EXPIRED_SESSION(403, "SESSION_001", "만료된 세션입니다."),

    // 처리하지 못한 예외
    INTERNAL_SERVER_ERROR(500, "SERVER_001", "서버 내부 오류가 발생했습니다.");

    private final int status;
    private final String code;
    private final String message;
}
