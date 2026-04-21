package koza.licensemanagementservice.domain.qna.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import koza.licensemanagementservice.domain.qna.entity.QnaPriority;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QnaPriorityUpdateRequest {
    @NotNull(message = "긴급도 값은 필수입니다.")
    @Schema(description = "변경할 긴급도", example = "URGENT")
    private QnaPriority priority;
}
