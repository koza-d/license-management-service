package koza.licensemanagementservice.domain.billing.toss.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TossPaymentRequest {
    @NotBlank(message = "고객 키는 필수입니다.")
    private String customerKey;

    @NotBlank(message = "주문 ID는 필수입니다.")
    @Size(max = 64, message = "주문 ID는 최대 64자입니다.")
    private String orderId;

    @NotBlank(message = "주문명은 필수입니다.")
    @Size(max = 255, message = "주문명은 최대 255자입니다.")
    private String orderName;

    @Min(value = 1, message = "결제 금액은 1원 이상이어야 합니다.")
    private int amount;
}
