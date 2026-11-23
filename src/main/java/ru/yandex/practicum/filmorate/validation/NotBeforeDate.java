package ru.yandex.practicum.filmorate.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotated {@link java.time.LocalDate} must not be earlier than {@link #value()}.
 * {@code null} is considered valid; combine with {@code @NotNull} if the value is required.
 */
@Documented
@Constraint(validatedBy = NotBeforeDateValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface NotBeforeDate {

    String value();

    String message() default "must not be before {value}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
