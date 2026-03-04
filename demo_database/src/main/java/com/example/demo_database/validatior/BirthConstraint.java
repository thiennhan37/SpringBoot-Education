package com.example.demo_database.validatior;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(
        validatedBy = {BirthValidator.class}
)
public @interface BirthConstraint {
    String message() default "{jakarta.validation.constraints.Size.message}";
    int min();
    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
