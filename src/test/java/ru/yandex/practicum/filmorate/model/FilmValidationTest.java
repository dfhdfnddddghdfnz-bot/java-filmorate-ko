package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import ru.yandex.practicum.filmorate.validation.OnUpdate;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.yandex.practicum.filmorate.model.Violations.fieldsOf;
import static ru.yandex.practicum.filmorate.model.Violations.messagesOf;

class FilmValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    void validate_validFilm_hasNoViolationsOnCreateAndUpdate() {
        Film film = validFilm();
        film.setId(1L);

        assertAll(
                () -> assertTrue(validator.validate(film).isEmpty(), "create"),
                () -> assertTrue(validator.validate(film, OnUpdate.class).isEmpty(), "update"));
    }

    @Test
    void validate_missingId_hasIdViolationOnlyOnUpdate() {
        Film film = validFilm();

        Set<ConstraintViolation<Film>> updateViolations = validator.validate(film, OnUpdate.class);

        assertAll(
                () -> assertTrue(validator.validate(film).isEmpty(), "create"),
                () -> assertEquals(Set.of("id"), fieldsOf(updateViolations)),
                () -> assertEquals(Set.of("must not be null"), messagesOf(updateViolations)));
    }

    @Test
    void validate_valuesOnValidBoundaries_hasNoViolations() {
        Film longestDescription = validFilm();
        longestDescription.setDescription("a".repeat(Film.MAX_DESCRIPTION_LENGTH));
        Film withoutDescription = validFilm();
        withoutDescription.setDescription(null);
        Film earliestRelease = validFilm();
        earliestRelease.setReleaseDate(LocalDate.of(1895, 12, 28));
        Film shortestDuration = validFilm();
        shortestDuration.setDuration(1);

        assertAll(
                () -> assertTrue(validator.validate(longestDescription).isEmpty(), "description of 200 chars"),
                () -> assertTrue(validator.validate(withoutDescription).isEmpty(), "no description"),
                () -> assertTrue(validator.validate(earliestRelease).isEmpty(), "release date 1895-12-28"),
                () -> assertTrue(validator.validate(shortestDuration).isEmpty(), "duration of 1 minute"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t\n"})
    void validate_blankName_hasNameViolation(String name) {
        Film film = validFilm();
        film.setName(name);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertAll(
                () -> assertEquals(Set.of("name"), fieldsOf(violations)),
                () -> assertEquals(Set.of("must not be blank"), messagesOf(violations)));
    }

    @Test
    void validate_descriptionLongerThanMax_hasDescriptionViolation() {
        Film film = validFilm();
        film.setDescription("a".repeat(Film.MAX_DESCRIPTION_LENGTH + 1));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertAll(
                () -> assertEquals(Set.of("description"), fieldsOf(violations)),
                () -> assertEquals(Set.of("must be at most 200 characters"), messagesOf(violations)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1895-12-27", "1890-03-25"})
    void validate_releaseDateBeforeEarliest_hasReleaseDateViolation(String releaseDate) {
        Film film = validFilm();
        film.setReleaseDate(LocalDate.parse(releaseDate));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertAll(
                () -> assertEquals(Set.of("releaseDate"), fieldsOf(violations)),
                () -> assertEquals(Set.of("must not be before 1895-12-28"), messagesOf(violations)));
    }

    @Test
    void validate_nullReleaseDate_hasReleaseDateViolation() {
        Film film = validFilm();
        film.setReleaseDate(null);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertAll(
                () -> assertEquals(Set.of("releaseDate"), fieldsOf(violations)),
                () -> assertEquals(Set.of("must not be null"), messagesOf(violations)));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -200})
    void validate_nonPositiveDuration_hasDurationViolation(int duration) {
        Film film = validFilm();
        film.setDuration(duration);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertAll(
                () -> assertEquals(Set.of("duration"), fieldsOf(violations)),
                () -> assertEquals(Set.of("must be positive"), messagesOf(violations)));
    }

    @Test
    void validate_nullDuration_hasDurationViolation() {
        Film film = validFilm();
        film.setDuration(null);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);

        assertAll(
                () -> assertEquals(Set.of("duration"), fieldsOf(violations)),
                () -> assertEquals(Set.of("must not be null"), messagesOf(violations)));
    }

    private static Film validFilm() {
        Film film = new Film();
        film.setName("Film name");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(1967, 3, 25));
        film.setDuration(100);
        return film;
    }
}
