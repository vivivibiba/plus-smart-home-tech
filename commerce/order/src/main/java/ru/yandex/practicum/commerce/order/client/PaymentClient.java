package ru.yandex.practicum.commerce.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.commerce.api.PaymentApi;

@FeignClient(name = "payment")
public interface PaymentClient extends PaymentApi {
}
