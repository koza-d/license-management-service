-- 회원 탈퇴 유예기간(14일) 기능 적용을 위한 마이그레이션
-- 적용 대상: 운영 DB (ddl-auto: none)
-- 선행조건: `members` 테이블 존재
-- 롤백 SQL은 파일 하단 참조

-- 1) 탈퇴 예정일 컬럼 추가
ALTER TABLE `members`
    ADD COLUMN `withdraw_scheduled_at` datetime(6) DEFAULT NULL
        AFTER `last_login_at`;

-- 2) status enum 에 PENDING_WITHDRAW 추가
ALTER TABLE `members`
    MODIFY COLUMN `status`
        enum('ACTIVE','BANNED','PENDING_WITHDRAW','WITHDRAW')
        COLLATE utf8mb4_unicode_ci NOT NULL;

-- 3) 스케줄러 풀스캔 방지용 복합 인덱스
--    (status='PENDING_WITHDRAW' AND withdraw_scheduled_at < NOW()) 조건 커버
ALTER TABLE `members`
    ADD KEY `idx_members_status_withdraw_scheduled_at`
        (`status`, `withdraw_scheduled_at`);


-- ─────────────────────────────────────────────────────────
-- 검증 쿼리 (적용 후 실행해서 결과 확인용)
-- ─────────────────────────────────────────────────────────

-- 컬럼/인덱스 적용 확인
-- SHOW CREATE TABLE `members`;

-- 탈퇴 예약 중인 회원 조회
-- SELECT id, email, status, withdraw_scheduled_at
--   FROM members
--  WHERE status = 'PENDING_WITHDRAW'
--  ORDER BY withdraw_scheduled_at;

-- 스케줄러 수동 검증: 예약된 회원의 예정일을 과거로 강제 → 다음 배치에서 익명화 대상이 됨
-- UPDATE members
--    SET withdraw_scheduled_at = NOW() - INTERVAL 1 MINUTE
--  WHERE id = <회원ID> AND status = 'PENDING_WITHDRAW';


-- ─────────────────────────────────────────────────────────
-- 롤백 (필요시)
-- ─────────────────────────────────────────────────────────
-- 주의: PENDING_WITHDRAW 상태인 회원이 존재하면 enum 축소 전에 상태를 ACTIVE 또는 WITHDRAW 로 먼저 정리해야 함
--
-- UPDATE members SET status = 'ACTIVE', withdraw_scheduled_at = NULL WHERE status = 'PENDING_WITHDRAW';
-- ALTER TABLE `members` DROP KEY `idx_members_status_withdraw_scheduled_at`;
-- ALTER TABLE `members` MODIFY COLUMN `status` enum('ACTIVE','BANNED','WITHDRAW') COLLATE utf8mb4_unicode_ci NOT NULL;
-- ALTER TABLE `members` DROP COLUMN `withdraw_scheduled_at`;
