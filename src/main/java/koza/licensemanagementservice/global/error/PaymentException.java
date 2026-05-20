package koza.licensemanagementservice.global.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public class PaymentException extends RuntimeException {
    private final HttpStatusCode status;
    private final String code;

    public PaymentException(HttpStatusCode status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }
}
