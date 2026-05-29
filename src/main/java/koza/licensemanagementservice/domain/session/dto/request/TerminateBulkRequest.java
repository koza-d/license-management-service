package koza.licensemanagementservice.domain.session.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

import java.util.List;

@Getter
public class TerminateBulkRequest {
    @NotEmpty(message = "세션 ID 목록은 필수입니다.")
    private List<String> ids;
}
