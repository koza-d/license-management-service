package koza.licensemanagementservice.global.error;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCode error;
    private final Object data;

    public BusinessException(ErrorCode error, Object data) {
        super(error.getMessage());
        this.error = error;
        this.data = data;
    }

    public BusinessException(ErrorCode error) {
        this(error, null);
    }

}
