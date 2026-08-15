package ru.yandex.practicum.commerce.payment.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.commerce.api.ShoppingStoreApi;

@FeignClient(name = "shopping-store")
public interface ShoppingStoreClient extends ShoppingStoreApi {
}
