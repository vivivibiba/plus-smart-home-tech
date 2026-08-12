package ru.yandex.practicum.commerce.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record AddProductToWarehouseRequest(
        @NotNull UUID productId,
        @NotNull @Positive Long quantity
) {
}
