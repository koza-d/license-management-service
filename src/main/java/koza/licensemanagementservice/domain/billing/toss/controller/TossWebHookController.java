package koza.licensemanagementservice.domain.billing.toss.controller;

import koza.licensemanagementservice.domain.billing.toss.dto.TossWebHookRequest;
import koza.licensemanagementservice.domain.payment.service.PendingPaymentResolverService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/webhook/toss")
@RequiredArgsConstructor
@Slf4j
public class TossWebHookController {

    private final PendingPaymentResolverService pendingPaymentResolverService;

    @PostMapping
    public ResponseEntity<Void> handleWebhook(@RequestBody TossWebHookRequest request) {
        log.info("토스 웹훅 수신 | eventType={} | orderId={}", request.getEventType(), request.getData().getOrderId());

        if ("PAYMENT_STATUS_CHANGED".equals(request.getEventType())) {
            TossWebHookRequest.Data data = request.getData();
            pendingPaymentResolverService.resolveByOrderId(data.getOrderId());
        }

        return ResponseEntity.ok().build();
    }
}
