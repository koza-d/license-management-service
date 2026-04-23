package koza.licensemanagementservice.domain.member.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberRole {
    USER("ROLE_USER"),
    ADMIN("ROLE_ADMIN");

    private final String authority;

    public static MemberRole from(java.util.List<String> authorities) {
        if (authorities != null && authorities.contains(ADMIN.authority)) {
            return ADMIN;
        }
        return USER;
    }
}
