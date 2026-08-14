package ru.yandex.practicum.commerce.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record CreateNewOrderRequest(
        @NotNull @Valid ShoppingCartDto shoppingCart,
        @NotNull @Valid AddressDto deliveryAddress
) {
}
