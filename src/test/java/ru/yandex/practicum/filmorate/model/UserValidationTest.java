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

class UserValidationTest {

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
    void validate_validUser_hasNoViolationsOnCreateAndUpdate() {
        User user = validUser();
        user.setId(1L);

        assertAll(
                () -> assertTrue(validator.validate(user).isEmpty(), "create"),
                () -> assertTrue(validator.validate(user, OnUpdate.class).isEmpty(), "update"));
    }

    @Test
    void validate_missingId_hasIdViolationOnlyOnUpdate() {
        User user = validUser();

        Set<ConstraintViolation<User>> updateViolations = validator.validate(user, OnUpdate.class);

        assertAll(
                () -> assertTrue(validator.validate(user).isEmpty(), "create"),
                () -> assertEquals(Set.of("id"), fieldsOf(updateViolations)),
                () -> assertEquals(Set.of("must not be null"), messagesOf(updateViolations)));
    }

    @Test
    void validate_valuesOnValidBoundaries_hasNoViolations() {
        User withoutName = validUser();
        withoutName.setName(null);
        User withEmptyName = validUser();
        withEmptyName.setName("");
        User bornToday = validUser();
        bornToday.setBirthday(LocalDate.now());

        assertAll(
                () -> assertTrue(validator.validate(withoutName).isEmpty(), "no name"),
                () -> assertTrue(validator.validate(withEmptyName).isEmpty(), "empty name"),
                () -> assertTrue(validator.validate(bornToday).isEmpty(), "born today"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void validate_nullOrEmptyEmail_hasNotBlankViolation(String email) {
        User user = validUser();
        user.setEmail(email);

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertAll(
                () -> assertEquals(Set.of("email"), fieldsOf(violations)),
                () -> assertEquals(Set.of("must not be blank"), messagesOf(violations)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"mail.ru", "this-is-invalid?email@", "@mail.ru", "user name@mail.ru"})
    void validate_malformedEmail_hasEmailViolation(String email) {
        User user = validUser();
        user.setEmail(email);

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertAll(
                () -> assertEquals(Set.of("email"), fieldsOf(violations)),
                () -> assertEquals(Set.of("must be a well-formed email address"), messagesOf(violations)));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void validate_nullOrEmptyLogin_hasNotBlankViolation(String login) {
        User user = validUser();
        user.setLogin(login);

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertAll(
                () -> assertEquals(Set.of("login"), fieldsOf(violations)),
                () -> assertEquals(Set.of("must not be blank"), messagesOf(violations)));
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "\t"})
    void validate_whitespaceOnlyLogin_hasNotBlankAndWhitespaceViolations(String login) {
        User user = validUser();
        user.setLogin(login);

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertAll(
                () -> assertEquals(Set.of("login"), fieldsOf(violations)),
                () -> assertEquals(Set.of("must not be blank", "must not contain whitespace"), messagesOf(violations)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"dolore ullamco", " login", "login ", "log\tin"})
    void validate_loginWithWhitespace_hasWhitespaceViolation(String login) {
        User user = validUser();
        user.setLogin(login);

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertAll(
                () -> assertEquals(Set.of("login"), fieldsOf(violations)),
                () -> assertEquals(Set.of("must not contain whitespace"), messagesOf(violations)));
    }

    @Test
    void validate_futureBirthday_hasBirthdayViolation() {
        User user = validUser();
        user.setBirthday(LocalDate.now().plusDays(1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertAll(
                () -> assertEquals(Set.of("birthday"), fieldsOf(violations)),
                () -> assertEquals(Set.of("must not be in the future"), messagesOf(violations)));
    }

    @Test
    void validate_nullBirthday_hasBirthdayViolation() {
        User user = validUser();
        user.setBirthday(null);

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertAll(
                () -> assertEquals(Set.of("birthday"), fieldsOf(violations)),
                () -> assertEquals(Set.of("must not be null"), messagesOf(violations)));
    }

    private static User validUser() {
        User user = new User();
        user.setEmail("mail@mail.ru");
        user.setLogin("dolore");
        user.setName("Nick Name");
        user.setBirthday(LocalDate.of(1946, 8, 20));
        return user;
    }
}
