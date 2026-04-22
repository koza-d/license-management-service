package koza.licensemanagementservice.domain.member.entity;

public enum JoinType {
    LOCAL, GOOGLE, GITHUB, NAVER, KAKAO;

    public static JoinType from(String provider) {
        if (provider == null || provider.isBlank()) {
            return LOCAL;
        }
        return JoinType.valueOf(provider.toUpperCase());
    }
}
