package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.repository.FilmRepository;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FilmServiceTest {

    private FilmService filmService;

    @BeforeEach
    void setUp() {
        filmService = new FilmService(new FilmRepository());
    }

    @Test
    void create_twoFilms_assignsSequentialIdsStartingFromOne() {
        Film first = filmService.create(film("First"));
        Film second = filmService.create(film("Second"));

        assertAll(
                () -> assertEquals(1L, first.getId()),
                () -> assertEquals(2L, second.getId()),
                () -> assertEquals(List.of("First", "Second"), storedNames()));
    }

    @Test
    void update_existingFilm_replacesStoredFilm() {
        Film created = filmService.create(film("Original"));
        Film changes = film("Updated");
        changes.setId(created.getId());

        Film updated = filmService.update(changes);

        assertAll(
                () -> assertEquals(created.getId(), updated.getId()),
                () -> assertEquals("Updated", updated.getName()),
                () -> assertEquals(List.of("Updated"), storedNames()));
    }

    @Test
    void update_unknownId_throwsNotFoundException() {
        filmService.create(film("Existing"));
        Film unknown = film("Unknown");
        unknown.setId(999L);

        assertAll(
                () -> assertEquals("Film with id=999 not found",
                        assertThrows(NotFoundException.class, () -> filmService.update(unknown)).getMessage()),
                () -> assertEquals(List.of("Existing"), storedNames()));
    }

    private List<String> storedNames() {
        return filmService.findAll().stream()
                .map(Film::getName)
                .toList();
    }

    private static Film film(String name) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(1967, 3, 25));
        film.setDuration(100);
        return film;
    }
}
