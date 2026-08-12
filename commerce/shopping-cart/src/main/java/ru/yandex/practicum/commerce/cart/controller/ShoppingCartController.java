package ru.yandex.practicum.commerce.cart.controller;

import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.api.ShoppingCartApi;
import ru.yandex.practicum.commerce.api.dto.ChangeProductQuantityRequest;
import ru.yandex.practicum.commerce.api.dto.ShoppingCartDto;
import ru.yandex.practicum.commerce.cart.service.ShoppingCartService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class ShoppingCartController implements ShoppingCartApi {
    private final ShoppingCartService service;

    public ShoppingCartController(ShoppingCartService service) {
        this.service = service;
    }

    @Override
    public ShoppingCartDto getCart(String username) {
        return service.getOrCreate(username);
    }

    @Override
    public ShoppingCartDto addProducts(String username, Map<UUID, Long> products) {
        return service.add(username, products);
    }

    @Override
    public ShoppingCartDto removeProducts(String username, List<UUID> productIds) {
        return service.remove(username, productIds);
    }

    @Override
    public ShoppingCartDto changeQuantity(String username, ChangeProductQuantityRequest request) {
        return service.changeQuantity(username, request);
    }

    @Override
    public void deactivate(String username) {
        service.deactivate(username);
    }
}
