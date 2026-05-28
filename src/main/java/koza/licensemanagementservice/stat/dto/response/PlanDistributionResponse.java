package koza.licensemanagementservice.stat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PlanDistributionResponse {
    private final Long free;
    private final Long pro;
    private final Long enterprise;
}
