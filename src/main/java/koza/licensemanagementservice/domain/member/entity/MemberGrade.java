package koza.licensemanagementservice.domain.member.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberGrade {
    BASIC(
            1,
            10
    ),
    PREMIUM(
            10,
            200
    ),
    ENTERPRISE(
            20,
            500
    );

    private final int limitLicense;
    private final int limitSoftware;
}
