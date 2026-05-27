package koza.licensemanagementservice.domain.billing.toss.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
@Getter
@RequiredArgsConstructor
public enum CardIssuer {

    IBK_BC("3K", "IBK_BC", "기업비씨"),
    GWANGJUBANK("46", "GWANGJUBANK", "광주"),
    LOTTE("71", "LOTTE", "롯데"),
    KDBBANK("30", "KDBBANK", "산업"),
    BC("31", "BC", "비씨"),
    SAMSUNG("51", "SAMSUNG", "삼성"),
    SAEMAUL("38", "SAEMAUL", "새마을"),
    SHINHAN("41", "SHINHAN", "신한"),
    SHINHYEOP("62", "SHINHYEOP", "신협"),
    CITI("36", "CITI", "씨티"),
    WOORI_BC("33", "WOORI", "우리"),
    WOORI("W1", "WOORI", "우리"),
    POST("37", "POST", "우체국"),
    SAVINGBANK("39", "SAVINGBANK", "저축"),
    JEONBUKBANK("35", "JEONBUKBANK", "전북"),
    JEJUBANK("42", "JEJUBANK", "제주"),
    KAKAOBANK("15", "KAKAOBANK", "카카오뱅크"),
    KBANK("3A", "KBANK", "케이뱅크"),
    TOSSBANK("24", "TOSSBANK", "토스뱅크"),
    HANA("21", "HANA", "하나"),
    HYUNDAI("61", "HYUNDAI", "현대"),
    KOOKMIN("11", "KOOKMIN", "국민"),
    NONGHYEOP("91", "NONGHYEOP", "농협"),
    SUHYEOP("34", "SUHYEOP", "수협"),
    PCP(null, "PCP", "페이코"),
    KBS(null, "KBS", "KB증권");

    private final String code;
    private final String englishName;
    private final String koreanName;

    public static String getKoreanName(String code) {
        if (code == null) return null;
        return Stream.of(values())
                .filter(i -> code.equals(i.code) || code.equals(i.englishName) || code.equals(i.name()))
                .findFirst()
                .map(CardIssuer::getKoreanName)
                .orElse(code);
    }
}