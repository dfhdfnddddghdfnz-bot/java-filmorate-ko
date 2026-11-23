package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    void findAll_existingUsers_returnsAllUsers() throws Exception {
        User user = validUser();
        user.setId(1L);
        when(userService.findAll()).thenReturn(List.of(user));

        mockMvc.perform(get("/users"))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.length()").value(1),
                        jsonPath("$[0].id").value(1),
                        jsonPath("$[0].login").value("dolore"));
    }

    @Test
    void create_validUser_returnsCreatedUser() throws Exception {
        when(userService.create(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(json(validUser())))
                .andExpectAll(
                        status().isCreated(),
                        jsonPath("$.id").value(1),
                        jsonPath("$.email").value("mail@mail.ru"),
                        jsonPath("$.login").value("dolore"),
                        jsonPath("$.name").value("Nick Name"),
                        jsonPath("$.birthday").value("1946-08-20"));
    }

    @Test
    void create_emptyBody_returnsBadRequestProblem() throws Exception {
        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
                        jsonPath("$.status").value(400),
                        jsonPath("$.instance").value("/users"));
    }

    @Test
    void create_invalidUser_returnsFieldViolations() throws Exception {
        User user = validUser();
        user.setEmail("this-is-invalid?email@");
        user.setLogin("dolore ullamco");
        user.setBirthday(LocalDate.of(2446, 8, 20));

        mockMvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(json(user)))
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
                        jsonPath("$.errors[*].field", containsInAnyOrder("email", "login", "birthday")),
                        jsonPath("$.errors[*].message", containsInAnyOrder(
                                "must be a well-formed email address",
                                "must not contain whitespace",
                                "must not be in the future")));
    }

    @Test
    void update_missingId_returnsIdViolation() throws Exception {
        mockMvc.perform(put("/users").contentType(MediaType.APPLICATION_JSON).content(json(validUser())))
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.errors.length()").value(1),
                        jsonPath("$.errors[0].field").value("id"),
                        jsonPath("$.errors[0].message").value("must not be null"));
    }

    @Test
    void update_unknownUser_returnsNotFoundProblem() throws Exception {
        User user = validUser();
        user.setId(999L);
        when(userService.update(any(User.class))).thenThrow(new NotFoundException("User with id=999 not found"));

        mockMvc.perform(put("/users").contentType(MediaType.APPLICATION_JSON).content(json(user)))
                .andExpectAll(
                        status().isNotFound(),
                        content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
                        jsonPath("$.status").value(404),
                        jsonPath("$.detail").value("User with id=999 not found"));
    }

    private String json(User user) throws Exception {
        return objectMapper.writeValueAsString(user);
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
