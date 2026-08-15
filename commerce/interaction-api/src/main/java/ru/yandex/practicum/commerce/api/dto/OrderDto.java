package ru.yandex.practicum.commerce.api.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import ru.yandex.practicum.commerce.api.model.OrderState;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public record OrderDto(
        @NotNull UUID orderId,
        UUID shoppingCartId,
        @NotEmpty Map<UUID, Long> products,
        UUID paymentId,
        UUID deliveryId,
        OrderState state,
        double deliveryWeight,
        double deliveryVolume,
        boolean fragile,
        BigDecimal totalPrice,
        BigDecimal deliveryPrice,
        BigDecimal productPrice
) {
    public OrderDto {
        products = products == null ? new LinkedHashMap<>() : new LinkedHashMap<>(products);
        totalPrice = totalPrice == null ? BigDecimal.ZERO : totalPrice;
        deliveryPrice = deliveryPrice == null ? BigDecimal.ZERO : deliveryPrice;
        productPrice = productPrice == null ? BigDecimal.ZERO : productPrice;
    }
}
