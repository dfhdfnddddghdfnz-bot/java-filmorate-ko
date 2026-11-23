package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;

import java.util.Set;
import java.util.stream.Collectors;

final class Violations {

    private Violations() {
    }

    static <T> Set<String> fieldsOf(Set<ConstraintViolation<T>> violations) {
        return violations.stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }

    static <T> Set<String> messagesOf(Set<ConstraintViolation<T>> violations) {
        return violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());
    }
}
