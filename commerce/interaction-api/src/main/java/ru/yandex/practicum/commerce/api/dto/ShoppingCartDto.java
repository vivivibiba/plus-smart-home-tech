package ru.yandex.practicum.commerce.api.dto;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public record ShoppingCartDto(UUID shoppingCartId, Map<UUID, Long> products) {
    public ShoppingCartDto {
        products = products == null ? new LinkedHashMap<>() : new LinkedHashMap<>(products);
    }
}
