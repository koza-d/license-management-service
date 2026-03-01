package koza.licensemanagementservice.verification.dto.resposne;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class HeartbeatResponse {
    private LocalDateTime exp;
    private Long remainMs;
}
