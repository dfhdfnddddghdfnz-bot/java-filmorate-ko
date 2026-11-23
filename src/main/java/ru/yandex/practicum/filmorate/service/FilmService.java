package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.repository.FilmRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmRepository filmRepository;

    public List<Film> findAll() {
        return filmRepository.findAll();
    }

    public Film create(Film film) {
        Film created = filmRepository.create(film);
        log.info("Film created: id={}, name='{}'", created.getId(), created.getName());
        return created;
    }

    public Film update(Film film) {
        Film updated = filmRepository.update(film)
                .orElseThrow(() -> new NotFoundException("Film with id=%d not found".formatted(film.getId())));
        log.info("Film updated: id={}, name='{}'", updated.getId(), updated.getName());
        return updated;
    }
}
