package ru.yandex.practicum.filmorate.repository;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class UserRepository {

    private final Map<Long, User> users = new ConcurrentSkipListMap<>();
    private final AtomicLong idSequence = new AtomicLong();

    public List<User> findAll() {
        return List.copyOf(users.values());
    }

    public User create(User user) {
        user.setId(idSequence.incrementAndGet());
        users.put(user.getId(), user);
        return user;
    }

    public Optional<User> update(User user) {
        return Optional.ofNullable(users.computeIfPresent(user.getId(), (id, stored) -> user));
    }
}
