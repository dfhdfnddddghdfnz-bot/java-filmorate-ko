package ru.yandex.practicum.filmorate.repository;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class FilmRepository {

    private final Map<Long, Film> films = new ConcurrentSkipListMap<>();
    private final AtomicLong idSequence = new AtomicLong();

    public List<Film> findAll() {
        return List.copyOf(films.values());
    }

    public Film create(Film film) {
        film.setId(idSequence.incrementAndGet());
        films.put(film.getId(), film);
        return film;
    }

    public Optional<Film> update(Film film) {
        return Optional.ofNullable(films.computeIfPresent(film.getId(), (id, stored) -> film));
    }
}
