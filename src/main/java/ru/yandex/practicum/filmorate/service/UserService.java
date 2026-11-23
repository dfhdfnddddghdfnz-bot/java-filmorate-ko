package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.UserRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User create(User user) {
        useLoginAsNameIfBlank(user);
        User created = userRepository.create(user);
        log.info("User created: id={}, login='{}'", created.getId(), created.getLogin());
        return created;
    }

    public User update(User user) {
        useLoginAsNameIfBlank(user);
        User updated = userRepository.update(user)
                .orElseThrow(() -> new NotFoundException("User with id=%d not found".formatted(user.getId())));
        log.info("User updated: id={}, login='{}'", updated.getId(), updated.getLogin());
        return updated;
    }

    private static void useLoginAsNameIfBlank(User user) {
        if (!StringUtils.hasText(user.getName())) {
            user.setName(user.getLogin());
        }
    }
}
