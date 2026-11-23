package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FilmController.class)
class FilmControllerTest {

    private static final String TRUNCATED_JSON = "{\"name\":";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FilmService filmService;

    @Test
    void findAll_existingFilms_returnsAllFilms() throws Exception {
        Film film = validFilm();
        film.setId(1L);
        when(filmService.findAll()).thenReturn(List.of(film));

        mockMvc.perform(get("/films"))
                .andExpectAll(
                        status().isOk(),
                        jsonPath("$.length()").value(1),
                        jsonPath("$[0].id").value(1),
                        jsonPath("$[0].name").value("Film name"));
    }

    @Test
    void create_validFilm_returnsCreatedFilm() throws Exception {
        when(filmService.create(any(Film.class))).thenAnswer(invocation -> {
            Film film = invocation.getArgument(0);
            film.setId(1L);
            return film;
        });

        mockMvc.perform(post("/films").contentType(MediaType.APPLICATION_JSON).content(json(validFilm())))
                .andExpectAll(
                        status().isCreated(),
                        jsonPath("$.id").value(1),
                        jsonPath("$.name").value("Film name"),
                        jsonPath("$.description").value("Description"),
                        jsonPath("$.releaseDate").value("1967-03-25"),
                        jsonPath("$.duration").value(100));
    }

    @Test
    void create_emptyBody_returnsBadRequestProblem() throws Exception {
        mockMvc.perform(post("/films").contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
                        jsonPath("$.status").value(400),
                        jsonPath("$.instance").value("/films"));
    }

    @Test
    void create_malformedJson_returnsBadRequestProblem() throws Exception {
        mockMvc.perform(post("/films").contentType(MediaType.APPLICATION_JSON).content(TRUNCATED_JSON))
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
                        jsonPath("$.status").value(400));
    }

    @Test
    void create_invalidFilm_returnsFieldViolations() throws Exception {
        Film film = validFilm();
        film.setName("");
        film.setDuration(-200);

        mockMvc.perform(post("/films").contentType(MediaType.APPLICATION_JSON).content(json(film)))
                .andExpectAll(
                        status().isBadRequest(),
                        content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
                        jsonPath("$.errors[*].field", containsInAnyOrder("name", "duration")),
                        jsonPath("$.errors[*].message", containsInAnyOrder("must not be blank", "must be positive")));
    }

    @Test
    void update_missingId_returnsIdViolation() throws Exception {
        mockMvc.perform(put("/films").contentType(MediaType.APPLICATION_JSON).content(json(validFilm())))
                .andExpectAll(
                        status().isBadRequest(),
                        jsonPath("$.errors.length()").value(1),
                        jsonPath("$.errors[0].field").value("id"),
                        jsonPath("$.errors[0].message").value("must not be null"));
    }

    @Test
    void update_unknownFilm_returnsNotFoundProblem() throws Exception {
        Film film = validFilm();
        film.setId(999L);
        when(filmService.update(any(Film.class))).thenThrow(new NotFoundException("Film with id=999 not found"));

        mockMvc.perform(put("/films").contentType(MediaType.APPLICATION_JSON).content(json(film)))
                .andExpectAll(
                        status().isNotFound(),
                        content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
                        jsonPath("$.status").value(404),
                        jsonPath("$.detail").value("Film with id=999 not found"));
    }

    @Test
    void findAll_unexpectedError_returnsInternalServerErrorProblem() throws Exception {
        when(filmService.findAll()).thenThrow(new IllegalStateException("Storage is broken"));

        mockMvc.perform(get("/films"))
                .andExpectAll(
                        status().isInternalServerError(),
                        content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
                        jsonPath("$.status").value(500),
                        jsonPath("$.detail").value("Unexpected server error"));
    }

    @Test
    void delete_unsupportedMethod_returnsMethodNotAllowedProblem() throws Exception {
        mockMvc.perform(delete("/films"))
                .andExpectAll(
                        status().isMethodNotAllowed(),
                        content().contentType(MediaType.APPLICATION_PROBLEM_JSON),
                        jsonPath("$.status").value(405));
    }

    private String json(Film film) throws Exception {
        return objectMapper.writeValueAsString(film);
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
