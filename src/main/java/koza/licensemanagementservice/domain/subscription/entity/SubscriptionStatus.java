package koza.licensemanagementservice.domain.subscription.entity;

public enum SubscriptionStatus {
    PENDING, // 결제대기 (구독 최초 생성 시만 존재할 수 있는 상태)
    DELETED, // 최초 결제 후 PG사 응답을 받아 실패확정인 경우 제거된 상태
    ACTIVE, // 활성
    EXPIRED, // 만료
    PAST_DUE, // 결제안됨(결제일 이후)
    CANCELLED, // 구독취소 (만료일까지 이용가능)
    REFUNDED, // 환불
}
