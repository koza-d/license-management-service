package koza.licensemanagementservice.global.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = JsonSizeValidator.class)
public @interface JsonSize {
    String message() default "변수 제한을 초과했습니다.";
    int maxKeys() default 10;
    int maxKeyLength() default 50;
    int maxValueLength() default 300;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
