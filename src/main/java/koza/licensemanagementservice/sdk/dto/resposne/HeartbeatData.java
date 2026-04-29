package koza.licensemanagementservice.sdk.dto.resposne;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class HeartbeatData {
    private LocalDateTime now;
    private LocalDateTime exp;
}
