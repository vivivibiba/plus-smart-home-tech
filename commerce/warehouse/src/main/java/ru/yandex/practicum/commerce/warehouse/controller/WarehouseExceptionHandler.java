package ru.yandex.practicum.commerce.warehouse.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.commerce.warehouse.exception.*;

import java.util.Map;

@RestControllerAdvice
public class WarehouseExceptionHandler {
    @ExceptionHandler(WarehouseProductNotFoundException.class)
    ResponseEntity<Map<String, String>> notFound(RuntimeException ex) {
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(ProductAlreadyExistsInWarehouseException.class)
    ResponseEntity<Map<String, String>> conflict(RuntimeException ex) {
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler({NotEnoughProductInWarehouseException.class, IllegalArgumentException.class, MethodArgumentNotValidException.class})
    ResponseEntity<Map<String, String>> badRequest(Exception ex) {
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }
}
