package koza.licensemanagementservice.domain.software.entity;

public enum SoftwareStatus {
    // 변경되는 곳
    // PATCH /api/software/status
    // PATCH /api/admin/software/status
    // 회원 MemberGrade 변경 시
    ACTIVE, // 활성
    BANNED, // 정지(관리자에 의한)
    INACTIVE, // 정지 해제 후 개발자의 확인 대기 상태 (관리자에 의한)
    SUSPENDED, // 시스템에 의한 일시정지 (플랜 다운그레이드 시 활성 소프트웨어 제한할 때)
    MAINTENANCE, // 점검 (개발자에 의한)
    UNSUPPORTED // 지원중단 (개발자에 의한)
}
