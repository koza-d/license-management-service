package koza.licensemanagementservice.global.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import koza.licensemanagementservice.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper objectMapper;
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        ErrorCode error = ErrorCode.ACCESS_DENIED;
        ApiResponse<?> apiResponse = ApiResponse.fail(error.getCode(), error.getMessage());

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(error.getStatus()); // 403
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
