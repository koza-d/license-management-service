package koza.licensemanagementservice.domain.license.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import koza.licensemanagementservice.domain.license.entity.LicenseStatus;

import static koza.licensemanagementservice.domain.license.entity.QLicense.license;

/**
 * 공통 License Querydsl 표현식 (라이센스 정책 등)
 */
public class LicenseExpressions {

    /**
     * 좌석 점유중인 라이센스 조건
     * @return
     */
    public static BooleanExpression isAllocated() {
        BooleanExpression isTemporaryBanned = license.status.eq(LicenseStatus.BANNED).and(license.statusUntil.isNotNull());
        return license.status.eq(LicenseStatus.ACTIVE).or(isTemporaryBanned);
    }

    /**
     * 좌석 미점유 중인 라이센스 조건
     * @return
     */
    public static BooleanExpression isUnAllocated() {
        BooleanExpression isPermanentBanned = license.status.eq(LicenseStatus.BANNED).and(license.statusUntil.isNull());
        return license.status.eq(LicenseStatus.INACTIVE)
                .or(license.status.eq(LicenseStatus.EXPIRED))
                .or(isPermanentBanned);
    }
}
