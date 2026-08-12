package ru.yandex.practicum.commerce.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ru.yandex.practicum.commerce.api.model.ProductCategory;
import ru.yandex.practicum.commerce.api.model.ProductState;
import ru.yandex.practicum.commerce.api.model.QuantityState;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductDto(
        UUID productId,
        @NotBlank String productName,
        @NotBlank String description,
        @NotBlank String imageSrc,
        QuantityState quantityState,
        ProductState productState,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal price,
        @NotNull ProductCategory productCategory
) {
}
