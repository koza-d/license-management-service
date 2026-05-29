package koza.licensemanagementservice.sdk.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class HeartbeatRequest {
    @NotBlank(message = "sessionId는 필수입니다.")
    @Size(max = 36, message = "sessionId는 최대 36자입니다.")
    private String sessionId;

    @NotBlank(message = "receivedSig는 필수입니다.")
    private String receivedSig;

    @NotNull(message = "receivedTs는 필수입니다.")
    private Long receivedTs;
}
