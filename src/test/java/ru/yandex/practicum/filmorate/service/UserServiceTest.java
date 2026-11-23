package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceTest {

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(new UserRepository());
    }

    @Test
    void create_twoUsers_assignsSequentialIdsStartingFromOne() {
        User first = userService.create(user("first", "First"));
        User second = userService.create(user("second", "Second"));

        assertAll(
                () -> assertEquals(1L, first.getId()),
                () -> assertEquals(2L, second.getId()),
                () -> assertEquals(List.of("first", "second"), storedLogins()));
    }

    @Test
    void create_nonBlankName_keepsName() {
        User created = userService.create(user("dolore", "Nick Name"));

        assertAll(
                () -> assertEquals("Nick Name", created.getName()),
                () -> assertEquals("dolore", created.getLogin()));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void create_blankName_usesLoginAsName(String name) {
        User created = userService.create(user("common", name));

        assertAll(
                () -> assertEquals("common", created.getName()),
                () -> assertEquals(List.of("common"), storedNames()));
    }

    @Test
    void update_existingUser_replacesStoredUser() {
        User created = userService.create(user("original", "Original"));
        User changes = user("updated", "Updated");
        changes.setId(created.getId());

        User updated = userService.update(changes);

        assertAll(
                () -> assertEquals(created.getId(), updated.getId()),
                () -> assertEquals("updated", updated.getLogin()),
                () -> assertEquals(List.of("Updated"), storedNames()));
    }

    @Test
    void update_blankName_usesLoginAsName() {
        User created = userService.create(user("original", "Original"));
        User changes = user("updated", "");
        changes.setId(created.getId());

        User updated = userService.update(changes);

        assertAll(
                () -> assertEquals("updated", updated.getName()),
                () -> assertEquals(List.of("updated"), storedNames()));
    }

    @Test
    void update_unknownId_throwsNotFoundException() {
        userService.create(user("existing", "Existing"));
        User unknown = user("unknown", "Unknown");
        unknown.setId(999L);

        assertAll(
                () -> assertEquals("User with id=999 not found",
                        assertThrows(NotFoundException.class, () -> userService.update(unknown)).getMessage()),
                () -> assertEquals(List.of("existing"), storedLogins()));
    }

    private List<String> storedLogins() {
        return userService.findAll().stream()
                .map(User::getLogin)
                .toList();
    }

    private List<String> storedNames() {
        return userService.findAll().stream()
                .map(User::getName)
                .toList();
    }

    private static User user(String login, String name) {
        User user = new User();
        user.setEmail(login + "@mail.ru");
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(LocalDate.of(1946, 8, 20));
        return user;
    }
}
