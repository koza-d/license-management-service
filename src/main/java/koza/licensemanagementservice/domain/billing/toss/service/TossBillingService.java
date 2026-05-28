package koza.licensemanagementservice.domain.billing.toss.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import koza.licensemanagementservice.domain.billing.toss.dto.*;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;
import koza.licensemanagementservice.global.error.PaymentException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import org.springframework.data.util.Pair;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class TossBillingService {

    @Value("${toss.secret-key}")
    private String secretKey;

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper;

    public TossBillingResponse issueBillingKey(TossBillingAuthRequest requestBody) {
        String auth = "Basic " + Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

        return restClient.post()
                .uri("https://api.tosspayments.com/v1/billing/authorizations/issue")
                .header(HttpHeaders.AUTHORIZATION, auth)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, resp) -> {
                    TossErrorResponse error = objectMapper.readValue(resp.getBody(), TossErrorResponse.class);
                    throw new PaymentException(resp.getStatusCode(), error.getCode(), error.getMessage());
                })
                .body(TossBillingResponse.class);
    }

    public Pair<TossPaymentResponse, String> getPaymentByOrderId(String orderId) {
        String auth = "Basic " + Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

        String rawResponse = restClient.get()
                .uri("https://api.tosspayments.com/v1/payments/orders/{orderId}", orderId)
                .header(HttpHeaders.AUTHORIZATION, auth)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, resp) -> {
                    TossErrorResponse error = objectMapper.readValue(resp.getBody(), TossErrorResponse.class);
                    throw new PaymentException(resp.getStatusCode(), error.getCode(), error.getMessage());
                })
                .body(String.class);

        try {
            TossPaymentResponse response = objectMapper.readValue(rawResponse, TossPaymentResponse.class);
            return Pair.of(response, rawResponse);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    public Pair<TossPaymentResponse, String> charge(String billingKey, TossPaymentRequest paymentRequest) {
        String auth = "Basic " + Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

        String rawResponse = restClient.post()
                .uri("https://api.tosspayments.com/v1/billing/{billingKey}", billingKey)
                .header(HttpHeaders.AUTHORIZATION, auth)
//                .header("TossPayments-Test-Code", "REJECT_CARD_PAYMENT")
                .contentType(MediaType.APPLICATION_JSON)
                .body(paymentRequest)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, resp) -> {
                    TossErrorResponse error = objectMapper.readValue(resp.getBody(), TossErrorResponse.class);
                    throw new PaymentException(resp.getStatusCode(), error.getCode(), error.getMessage());
                })
                .body(String.class);

        try {
            TossPaymentResponse response = objectMapper.readValue(rawResponse, TossPaymentResponse.class);
            return Pair.of(response, rawResponse);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
