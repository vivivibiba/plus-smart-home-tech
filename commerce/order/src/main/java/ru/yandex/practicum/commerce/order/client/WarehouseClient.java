package ru.yandex.practicum.commerce.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.commerce.api.WarehouseApi;

@FeignClient(name = "warehouse")
public interface WarehouseClient extends WarehouseApi {
}
