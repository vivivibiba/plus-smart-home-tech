package ru.yandex.practicum.commerce.api.dto;

public record BookedProductsDto(
        double deliveryWeight,
        double deliveryVolume,
        boolean fragile
) {
}
