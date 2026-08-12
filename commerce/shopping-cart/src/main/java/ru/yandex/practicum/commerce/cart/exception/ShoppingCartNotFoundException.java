package ru.yandex.practicum.commerce.cart.exception;

public class ShoppingCartNotFoundException extends RuntimeException {
    public ShoppingCartNotFoundException(String username) {
        super("Shopping cart not found for user: " + username);
    }
}
