package koza.licensemanagementservice.domain.session.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SessionTerminateRequest {
    @Size(max = 500, message = "종료 사유는 500자를 넘을 수 없습니다.")
    @Schema(description = "종료 사유", example = "세션 비정상 사용")
    private String reason;
}
