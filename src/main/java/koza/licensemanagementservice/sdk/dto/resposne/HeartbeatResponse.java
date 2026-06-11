package koza.licensemanagementservice.sdk.dto.resposne;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HeartbeatResponse {
    private Long serverSeq;
    private String encryptedData; // HeartbeatData 직렬화 -> 암호화한 문자열
}
