package ru.yandex.practicum.filmorate.validation;

import jakarta.validation.groups.Default;

/**
 * Validation group for update requests.
 *
 * <p>Extends {@link Default}, so validating with this group checks all default constraints
 * plus the ones declared with {@code groups = OnUpdate.class}.
 */
public interface OnUpdate extends Default {
}
