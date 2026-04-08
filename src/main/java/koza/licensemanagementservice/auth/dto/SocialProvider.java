package koza.licensemanagementservice.auth.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum SocialProvider {
    GOOGLE(
            "google",
            "https://accounts.google.com/o/oauth2/auth",
            "https://oauth2.googleapis.com/token",
            "https://www.googleapis.com/oauth2/v2/userinfo",
            "email profile"
    ),
    GITHUB(
            "github",
            "https://github.com/login/oauth/authorize",
            "https://github.com/login/oauth/access_token",
            "https://api.github.com/user",
            "email profile"
    ),

    NAVER(
            "naver",
            "https://nid.naver.com/oauth2.0/authorize",
            "https://nid.naver.com/oauth2.0/token",
            "https://openapi.naver.com/v1/nid/me",
            "name email profile_image"
    ),
    KAKAO(
            "kakao",
            "https://kauth.kakao.com/oauth/authorize",
            "https://kauth.kakao.com/oauth/token",
            "https://kapi.kakao.com/v2/user/me",
            ""
    ),

    ;
    private final String name;
    private final String authURL; // 인증 페이지 URL
    private final String tokenURL; // 토큰 요청 URL
    private final String userInfoURL; // 유저 정보 요청 URL
    private final String scope; // 공개범위

}
