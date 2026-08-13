package ru.yandex.practicum.commerce.warehouse.exception;

import java.util.UUID;

public class WarehouseProductNotFoundException extends RuntimeException {
    public WarehouseProductNotFoundException(UUID id) {
        super("Product is absent in warehouse: " + id);
    }
}
