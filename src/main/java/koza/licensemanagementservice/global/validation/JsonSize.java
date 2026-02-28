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
    String message() default "크기가 너무 큽니다.";
    int max() default 5000;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}