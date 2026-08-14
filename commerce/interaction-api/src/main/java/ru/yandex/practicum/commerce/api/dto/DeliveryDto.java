package ru.yandex.practicum.commerce.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import ru.yandex.practicum.commerce.api.model.DeliveryState;

import java.util.UUID;

public record DeliveryDto(
        UUID deliveryId,
        @NotNull @Valid AddressDto fromAddress,
        @NotNull @Valid AddressDto toAddress,
        @NotNull UUID orderId,
        DeliveryState deliveryState
) {
}
