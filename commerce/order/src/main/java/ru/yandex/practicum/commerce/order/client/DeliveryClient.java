package ru.yandex.practicum.commerce.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.commerce.api.DeliveryApi;

@FeignClient(name = "delivery")
public interface DeliveryClient extends DeliveryApi {
}
