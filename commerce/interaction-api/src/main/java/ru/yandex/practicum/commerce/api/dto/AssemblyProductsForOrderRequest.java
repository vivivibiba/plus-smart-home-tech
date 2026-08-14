package ru.yandex.practicum.commerce.api.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public record AssemblyProductsForOrderRequest(
        @NotEmpty Map<UUID, Long> products,
        @NotNull UUID orderId
) {
}
