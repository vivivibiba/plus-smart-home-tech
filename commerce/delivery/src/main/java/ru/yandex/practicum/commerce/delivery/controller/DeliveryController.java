package ru.yandex.practicum.commerce.delivery.controller;

import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.api.DeliveryApi;
import ru.yandex.practicum.commerce.api.dto.DeliveryDto;
import ru.yandex.practicum.commerce.api.dto.OrderDto;
import ru.yandex.practicum.commerce.delivery.service.DeliveryService;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
public class DeliveryController implements DeliveryApi {
    private final DeliveryService service;

    public DeliveryController(DeliveryService service) { this.service = service; }

    @Override public DeliveryDto planDelivery(DeliveryDto delivery) { return service.plan(delivery); }
    @Override public void deliverySuccessful(UUID deliveryId) { service.successful(deliveryId); }
    @Override public void deliveryPicked(UUID deliveryId) { service.picked(deliveryId); }
    @Override public void deliveryFailed(UUID deliveryId) { service.failed(deliveryId); }
    @Override public BigDecimal deliveryCost(OrderDto order) { return service.cost(order); }
}
