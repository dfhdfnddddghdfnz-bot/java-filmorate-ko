package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import ru.yandex.practicum.filmorate.validation.NotBeforeDate;
import ru.yandex.practicum.filmorate.validation.OnUpdate;

import java.time.LocalDate;

@Getter
@Setter
public class Film {

    public static final int MAX_DESCRIPTION_LENGTH = 200;
    public static final String EARLIEST_RELEASE_DATE = "1895-12-28";

    @NotNull(groups = OnUpdate.class, message = "must not be null")
    private Long id;

    @NotBlank(message = "must not be blank")
    private String name;

    @Size(max = MAX_DESCRIPTION_LENGTH, message = "must be at most {max} characters")
    private String description;

    @NotNull(message = "must not be null")
    @NotBeforeDate(EARLIEST_RELEASE_DATE)
    private LocalDate releaseDate;

    @NotNull(message = "must not be null")
    @Positive(message = "must be positive")
    private Integer duration;
}
