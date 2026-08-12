package ru.yandex.practicum.commerce.api.dto;

import jakarta.validation.constraints.NotNull;
import ru.yandex.practicum.commerce.api.model.QuantityState;

import java.util.UUID;

public record SetProductQuantityStateRequest(
        @NotNull UUID productId,
        @NotNull QuantityState quantityState
) {
}
