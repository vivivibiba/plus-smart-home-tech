package ru.yandex.practicum.commerce.warehouse.exception;

import java.util.UUID;

public class ProductAlreadyExistsInWarehouseException extends RuntimeException {
    public ProductAlreadyExistsInWarehouseException(UUID id) {
        super("Product already exists in warehouse: " + id);
    }
}
