package koza.licensemanagementservice.global.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import koza.licensemanagementservice.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        ErrorCode error = ErrorCode.UNAUTHORIZED;
        ApiResponse<?> apiResponse = ApiResponse.fail(error.getCode(), error.getMessage());

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(error.getStatus()); // 401
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
