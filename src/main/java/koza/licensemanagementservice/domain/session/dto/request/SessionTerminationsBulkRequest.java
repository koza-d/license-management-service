package koza.licensemanagementservice.domain.session.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SessionTerminationsBulkRequest {
    @NotEmpty(message = "종료할 세션 ID를 하나 이상 입력해주세요.")
    @Schema(description = "종료할 세션 ID 목록 (UUID 문자열)")
    private List<String> ids;

    @Size(max = 500, message = "종료 사유는 500자를 넘을 수 없습니다.")
    @Schema(description = "종료 사유", example = "일괄 종료")
    private String reason;
}
