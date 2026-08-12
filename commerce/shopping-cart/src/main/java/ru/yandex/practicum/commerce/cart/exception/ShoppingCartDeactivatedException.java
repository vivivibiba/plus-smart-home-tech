package ru.yandex.practicum.commerce.cart.exception;

public class ShoppingCartDeactivatedException extends RuntimeException {
    public ShoppingCartDeactivatedException(String username) {
        super("Shopping cart is deactivated for user: " + username);
    }
}
