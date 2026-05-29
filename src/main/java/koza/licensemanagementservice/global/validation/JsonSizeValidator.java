package koza.licensemanagementservice.global.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Map;

public class JsonSizeValidator implements ConstraintValidator<JsonSize, Map<String, String>> {
    private int maxKeys;
    private int maxKeyLength;
    private int maxValueLength;

    @Override
    public void initialize(JsonSize constraintAnnotation) {
        this.maxKeys = constraintAnnotation.maxKeys();
        this.maxKeyLength = constraintAnnotation.maxKeyLength();
        this.maxValueLength = constraintAnnotation.maxValueLength();
    }

    @Override
    public boolean isValid(Map<String, String> value, ConstraintValidatorContext context) {
        if (value == null) return true;

        if (value.size() > maxKeys) {
            setMessage(context, "변수 개수는 최대 " + maxKeys + "개까지 허용됩니다.");
            return false;
        }

        for (Map.Entry<String, String> entry : value.entrySet()) {
            if (entry.getKey() != null && entry.getKey().length() > maxKeyLength) {
                setMessage(context, "변수 키는 최대 " + maxKeyLength + "자까지 허용됩니다. (키: " + entry.getKey() + ")");
                return false;
            }
            if (entry.getValue() != null && entry.getValue().length() > maxValueLength) {
                setMessage(context, "변수 값은 최대 " + maxValueLength + "자까지 허용됩니다. (키: " + entry.getKey() + ")");
                return false;
            }
        }

        return true;
    }

    private void setMessage(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
}
