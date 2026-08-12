package ru.yandex.practicum.commerce.cart.controller;

import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.commerce.cart.exception.ShoppingCartDeactivatedException;
import ru.yandex.practicum.commerce.cart.exception.ShoppingCartNotFoundException;

import java.util.Map;

@RestControllerAdvice
public class ShoppingCartExceptionHandler {
    @ExceptionHandler(ShoppingCartNotFoundException.class)
    ResponseEntity<Map<String, String>> notFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler({ShoppingCartDeactivatedException.class, IllegalArgumentException.class, MethodArgumentNotValidException.class})
    ResponseEntity<Map<String, String>> badRequest(Exception ex) {
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(FeignException.class)
    ResponseEntity<Map<String, String>> warehouseError(FeignException ex) {
        HttpStatus status = HttpStatus.resolve(ex.status());
        if (status == null) status = HttpStatus.BAD_GATEWAY;
        return ResponseEntity.status(status).body(Map.of("message", "Warehouse request failed"));
    }
}
