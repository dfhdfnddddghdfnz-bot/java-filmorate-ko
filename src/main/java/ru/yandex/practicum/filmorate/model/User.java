package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import ru.yandex.practicum.filmorate.validation.OnUpdate;

import java.time.LocalDate;

@Getter
@Setter
public class User {

    @NotNull(groups = OnUpdate.class, message = "must not be null")
    private Long id;

    @NotBlank(message = "must not be blank")
    @Email(message = "must be a well-formed email address")
    private String email;

    @NotBlank(message = "must not be blank")
    @Pattern(regexp = "\\S*", message = "must not contain whitespace")
    private String login;

    private String name;

    @NotNull(message = "must not be null")
    @PastOrPresent(message = "must not be in the future")
    private LocalDate birthday;
}
