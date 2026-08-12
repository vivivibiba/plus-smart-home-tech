package ru.yandex.practicum.commerce.warehouse.exception;

import java.util.Collection;
import java.util.UUID;

public class NotEnoughProductInWarehouseException extends RuntimeException {
    public NotEnoughProductInWarehouseException(Collection<UUID> ids) {
        super("Not enough products in warehouse: " + ids);
    }
}
