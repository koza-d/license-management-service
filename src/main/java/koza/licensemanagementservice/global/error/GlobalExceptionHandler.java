package koza.licensemanagementservice.global.error;

import koza.licensemanagementservice.global.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    // Validation 예외 처리 부분, 입력값 검증 실패 시
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationException(MethodArgumentNotValidException e) {
        HashMap<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        ApiResponse<?> response = ApiResponse.fail(
                ErrorCode.INVALID_INPUT_VALUE.getCode(),
                ErrorCode.INVALID_INPUT_VALUE.getMessage(),
                errors
        );

        return ResponseEntity.badRequest().body(response);
    }

    // 서버 내에서 의도된 예외 처리
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<?>> handleBusinessException(BusinessException e) {
        ErrorCode error = e.getError();
        ApiResponse<?> response = ApiResponse.fail(error.getCode(), error.getMessage());
        return ResponseEntity.status(error.getStatus()).body(response);
    }

    // 처리되지 않은 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleException(Exception e) {
        ErrorCode error = ErrorCode.INTERNAL_SERVER_ERROR;
        ApiResponse<?> response = ApiResponse.fail(
                error.getCode(),
                error.getMessage()
        );
        log.warn(e.getMessage());
        e.printStackTrace();
        return ResponseEntity.status(error.getStatus()).body(response);
    }
}
