package koza.licensemanagementservice.sdk.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VerifyRequest {
    @NotBlank(message = "publicKey는 필수입니다.")
    private String publicKey;

    @NotBlank(message = "licenseKey는 필수입니다.")
    @Size(max = 128, message = "licenseKey는 최대 128자입니다.")
    private String licenseKey;

    @NotBlank(message = "appId는 필수입니다.")
    @Size(max = 10, message = "appId는 최대 10자입니다.")
    private String appId;

    @NotBlank(message = "clientVersion은 필수입니다.")
    @Size(max = 20, message = "clientVersion은 최대 20자입니다.")
    private String clientVersion;

    @Size(max = 64, message = "fileHash는 최대 64자입니다.")
    private String fileHash;
}
