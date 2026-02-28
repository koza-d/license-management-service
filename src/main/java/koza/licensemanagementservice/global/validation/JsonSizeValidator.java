package koza.licensemanagementservice.global.validation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import koza.licensemanagementservice.global.error.BusinessException;
import koza.licensemanagementservice.global.error.ErrorCode;

import java.util.Map;

public class JsonSizeValidator implements ConstraintValidator<JsonSize, Map<String, Object>> {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private int max;

    @Override
    public void initialize(JsonSize constraintAnnotation) {
        this.max = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(Map<String, Object> value, ConstraintValidatorContext context) {
        if (value == null) return true; // null 체크는 @NotNull에 맡김
        try {
            String json = objectMapper.writeValueAsString(value);
            return json.length() <= max;
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.METADATA_FORMAT_WRONG);
        }
    }
}
