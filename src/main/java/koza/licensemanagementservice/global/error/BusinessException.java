package koza.licensemanagementservice.global.error;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCode error;

    public BusinessException(ErrorCode error) {
        super(error.getMessage());
        this.error = error;
    }
}
