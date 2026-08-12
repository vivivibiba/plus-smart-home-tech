package ru.yandex.practicum.commerce.api.dto;

import jakarta.validation.constraints.Positive;

public record DimensionDto(
        @Positive double width,
        @Positive double height,
        @Positive double depth
) {
    public double volume() {
        return width * height * depth;
    }
}
