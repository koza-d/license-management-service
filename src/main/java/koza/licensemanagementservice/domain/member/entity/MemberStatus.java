package koza.licensemanagementservice.domain.member.entity;

public enum MemberStatus {
    ACTIVE,
    BANNED,
    PENDING_WITHDRAW, // 탈퇴 예약 (유예기간 중)
    WITHDRAW // 익명화 완료
}
