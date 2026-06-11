package koza.licensemanagementservice.sdk.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ChangeLocalVariablesRequest {
    @NotBlank(message = "sessionId는 필수입니다.")
    @Size(max = 36, message = "sessionId는 최대 36자입니다.")
    private String sessionId;
    private Long clientSeq; // SDK 내부에서 매 요청마다 increase 되는 sequence
    private String key;
    private String value;
    private String encryptData; // sessionId, clientSeq, key, value 암호화(변조감지용)
}
