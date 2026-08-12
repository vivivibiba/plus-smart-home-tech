package ru.yandex.practicum.commerce.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record NewProductInWarehouseRequest(
        @NotNull UUID productId,
        boolean fragile,
        @NotNull @Valid DimensionDto dimension,
        @Positive double weight
) {
}
