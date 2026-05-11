package koza.licensemanagementservice.sdk.dto.resposne;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InitResponse {
    private String softwareName;
    private String latestVersion;
    private String clientVersion;
    private String downloadURL;
    private String sig;
    private String ts;
}
