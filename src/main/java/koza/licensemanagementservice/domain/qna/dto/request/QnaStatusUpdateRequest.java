package koza.licensemanagementservice.domain.qna.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import koza.licensemanagementservice.domain.qna.entity.QnaStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QnaStatusUpdateRequest {
    @NotNull(message = "상태값은 필수입니다.")
    @Schema(description = "변경할 상태", example = "CLOSED")
    private QnaStatus status;
}
