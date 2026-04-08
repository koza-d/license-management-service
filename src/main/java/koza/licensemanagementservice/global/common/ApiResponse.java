package koza.licensemanagementservice.global.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    private boolean success;
    private T data;
    ErrorResponse error;
    
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }
    
    // 일반적인 에러
    public static ApiResponse<?> fail(String code, String message) {
        return new ApiResponse<>(false, null, new ErrorResponse(code, message, null));
    }

    // Validation 에러 시 호출
    // errors에는 필드명과 에러메시지가 각각 들어감
    public static ApiResponse<?> fail(String code, String message, Map<String, String> errors) {
        return new ApiResponse<>(false, null, new ErrorResponse(code, message, errors));
    }

    public static <T> ApiResponse<T> fail(String code, String message, T data) {
        return new ApiResponse<>(false, data, new ErrorResponse(code, message, null));
    }


    @Getter
    @AllArgsConstructor
    public static class ErrorResponse {
        private String code;
        private String message;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private Map<String, String> errors;
    }
}
