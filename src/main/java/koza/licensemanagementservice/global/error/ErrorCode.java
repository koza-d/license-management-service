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
    INVALID_REQUEST(400, "COMMON_006", "잘못된 요청입니다."),

    // OAuth 관련 예외
    EMAIL_NOT_REGISTERED(404, "OAUTH_001", "가입되지 않은 이메일입니다."),
    OAUTH_NOT_REGISTERED(404, "OAUTH_002", "선택한 소셜로 가입된 이메일이 아닙니다."),

    // 회원 도메인 관련 예외
    DUPLICATE_EMAIL(409, "MEMBER_001", "이미 가입된 이메일입니다."),
    INCORRECT_EMAIL_OR_PASSWORD(401, "MEMBER_002", "이메일 또는 비밀번호가 유효하지 않습니다."),
    MEMBER_NOT_FOUND(404, "MEMBER_003", "회원을 찾을 수 없습니다."),
    MEMBER_STATUS_SAME(409, "MEMBER_004", "현재 상태와 동일한 상태로 변경할 수 없습니다."),
    MEMBER_GRADE_SAME(409, "MEMBER_005", "현재 등급과 동일한 등급으로 변경할 수 없습니다."),
    MEMBER_BANNED(403, "MEMBER_006", "정지된 계정입니다. 관리자에게 문의하세요."),
    MEMBER_ROLE_SAME(400, "MEMBER_007", "현재 역할과 동일한 역할로 변경할 수 없습니다."),
    MEMBER_ROLE_SELF_FORBIDDEN(400, "MEMBER_008", "본인의 역할은 변경할 수 없습니다."),

    // 소프트웨어 관련 예외
    SOFTWARE_NOT_FOUND(404, "SOFTWARE_001", "소프트웨어를 찾을 수 없습니다."),
    SOFTWARE_STATUS_SAME(409, "SOFTWARE_002", "현재 상태와 동일한 상태로 변경할 수 없습니다."),
    SOFTWARE_BANNED(403, "SOFTWARE_003", "소프트웨어가 관리자에 의해 정지된 상태입니다."),
    SOFTWARE_NOT_BANNED(409, "SOFTWARE_004", "소프트웨어가 밴 상태가 아닙니다."),
    SOFTWARE_NOT_ACTIVE(409, "SOFTWARE_005", "소프트웨어가 활성 상태가 아닙니다."),

    // 소프트웨어 버전 관련 예외
    DUPLICATE_VERSION(409, "VERSION_001", "이미 등록돼있는 버전입니다."),
    NOT_FOUND_LATEST_VERSION(404, "VERSION_002", "최신버전을 찾을 수 없습니다."),

    // 라이센스 관련 예외
    LICENSE_NOT_FOUND(404, "LICENSE_001", "라이센스를 찾을 수 없습니다."),
    LICENSE_BANNED(403, "LICENSE_002", "라이센스가 정지된 상태입니다."),
    LICENSE_NOT_BANNED(409, "LICENSE_003", "라이센스가 밴 상태가 아닙니다."),
    LICENSE_NOT_ACTIVATED(409, "LICENSE_004", "아직 활성화되지 않은 라이센스입니다."),
    LICENSE_NOT_INACTIVATED(409, "LICENSE_004", "비활성화된 라이센스가 아닙니다."),


    // 세션 로직 예외
    EXPIRED_SESSION(403, "SESSION_001", "만료된 세션입니다."),
    SESSION_NOT_FOUND(404, "SESSION_002", "세션을 찾을 수 없습니다."),
    SESSION_ALREADY_TERMINATED(409, "SESSION_003", "이미 종료된 세션입니다."),

    // QNA 예외
    QNA_NOT_FOUND(404, "QNA_001", "질문을 찾을 수 없습니다."),
    QNA_LICENSE_NOT_ACTIVE(403, "QNA_003", "유효하지 않은 라이센스입니다. 활성 상태의 라이센스만 질문할 수 있습니다."),

    // FAQ 예외
    FAQ_NOT_FOUND(404, "FAQ_001", "FAQ를 찾을 수 없습니다."),

    // 결제 관련 예외
    PAYMENT_METHOD_DUPLICATE(409, "PAYMENT_001", "이미 등록된 결제수단입니다."),
    PAYMENT_METHOD_CANNOT_DELETE_DEFAULT(400, "PAYMENT_002", "기본값으로 설정된 결제수단이라 삭제할 수 없습니다."),
    PAYMENT_METHOD_NOT_FOUND(400, "PAYMENT_003", "등록된 결제수단을 찾을 수 없습니다."),
    PAYMENT_METHOD_NOT_ACTIVE(400, "PAYMENT_004", "설정된 결제수단이 사용할 수 없는 상태입니다."),
    PAYMENT_NOT_FOUND(404, "PAYMENT_005", "결제를 찾을 수 없습니다."),
    PAYMENT_NOT_PENDING(409, "PAYMENT_006", "대기 상태의 결제만 처리할 수 있습니다."),
    PAYMENT_NOT_SUCCESS(409, "PAYMENT_007", "성공 상태의 결제만 환불할 수 있습니다."),
    PAYMENT_REFUND_FAILED(500, "PAYMENT_008", "환불 처리 중 오류가 발생했습니다."),
    PAYMENT_ALREADY_RESOLVE(409, "PAYMENT_009", "이미 처리된 결제입니다."),
    PAYMENT_REFUNDED_BELATED(400, "PAYMENT_010", "결제시점으로부터 7일이 지나 환불이 불가능합니다."),

    // 구독 관련 예외
    SUBSCRIPTION_RENEWAL_TARGET_NOT_FOUND(404, "SUBSCRIPTION_001", "갱신할 구독을 찾을 수 없습니다."),
    SUBSCRIPTION_ALREADY_ACTIVE(409, "SUBSCRIPTION_002", "이미 활성 중인 구독이 있습니다."),
    SUBSCRIPTION_PAYMENT_PENDING(409, "SUBSCRIPTION_003", "결제가 처리 중입니다. 잠시 후 다시 확인해주세요. 문제가 지속되면 문의 바랍니다."),
    SUBSCRIPTION_RENEWAL_PAYMENT_SUCCESS(409, "SUBSCRIPTION_004", "구독 갱신 결제가 이미 처리됐습니다."),
    SUBSCRIPTION_RENEWAL_EARLY(400, "SUBSCRIPTION_005", "구독 갱신은 구독 종료 7일전부터 가능합니다."),
    SUBSCRIPTION_NOT_FOUND(404, "SUBSCRIPTION_006", "구독을 찾을 수 없습니다."),
    SUBSCRIPTION_NOT_ACTIVE_PAST_DUE(400, "SUBSCRIPTION_007", "구독이 활성화된 상태가 아닙니다."),

    // 처리하지 못한 예외
    INTERNAL_SERVER_ERROR(500, "SERVER_001", "서버 내부 오류가 발생했습니다."),

    // SDK 전용 예외
    SDK_SERVER_ERROR(500, "SDK_001", "서버 내부 오류가 발생했습니다."),
    SDK_INVALID_REQUEST(400, "SDK_002", "유효하지 않은 요청입니다."),
    SDK_INVALID_SOFTWARE(404, "SDK_003", "유효하지 않은 소프트웨어입니다."),
    SDK_INVALID_LICENSE(404, "SDK_004", "유효하지 않은 라이센스입니다."),
    SDK_INVALID_FILE_HASH(400, "SDK_005", "변조된 파일입니다."),
    SDK_NOT_AVAILABLE_VERSION(400, "SDK_006", "사용 불가능한 버전입니다."),

    SDK_LICENSE_IN_USE(409, "SDK_101", "이미 사용 중인 라이센스입니다."),
    SDK_LICENSE_BANNED(403, "SDK_102", "사용 정지된 라이센스입니다."),
    SDK_LICENSE_EXPIRED(403, "SDK_103", "만료된 라이센스입니다."),

    SDK_SOFTWARE_BANNED(403, "SDK_201", "사용 정지된 소프트웨어입니다."), // 정지 종료 일시, 정지 사유 반환 필요
    SDK_SOFTWARE_INACTIVE(403, "SDK_202", "비활성 상태인 소프트웨어입니다. 해당 소프트웨어 관리자에게 문의하세요."),

    SDK_SOFTWARE_MAINTENANCE(403, "SDK_204", "소프트웨어가 점검중입니다."), // 점검 종료 일시 반환 필요
    SDK_SOFTWARE_UNSUPPORTED(403, "SDK_205", "지원중단된 소프트웨어입니다."), // 지원중단 사유 반환 필요

    SDK_SESSION_EXPIRED(403, "SDK_301", "만료된 세션입니다."),

    SDK_VARIABLE_NULL(400, "SDK_401", "로컬변수의 키 또는 값이 지정되지 않았습니다."),
    SDK_VARIABLE_KEY_MAX(400, "SDK_402", "로컬변수의 키가 제한된 크기를 초과했습니다."),
    SDK_VARIABLE_VALUE_MAX(400, "SDK_403", "로컬변수의 값이 제한된 크기를 초과했습니다."),
    SDK_VARIABLE_COUNT_MAX(400, "SDK_404", "설정할 수 있는 로컬변수의 개수가 초과했습니다.")
    ;

    private final int status;
    private final String code;
    private final String message;
}
