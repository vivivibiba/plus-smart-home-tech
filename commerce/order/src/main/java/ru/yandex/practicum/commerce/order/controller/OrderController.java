package ru.yandex.practicum.commerce.order.controller;

import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.api.OrderApi;
import ru.yandex.practicum.commerce.api.dto.CreateNewOrderRequest;
import ru.yandex.practicum.commerce.api.dto.OrderDto;
import ru.yandex.practicum.commerce.api.dto.ProductReturnRequest;
import ru.yandex.practicum.commerce.order.service.OrderService;

import java.util.List;
import java.util.UUID;

@RestController
public class OrderController implements OrderApi {
    private final OrderService service;

    public OrderController(OrderService service) { this.service = service; }

    @Override public List<OrderDto> getClientOrders(String username) { return service.getClientOrders(username); }
    @Override public OrderDto createNewOrder(CreateNewOrderRequest request) { return service.create(request); }
    @Override public OrderDto productReturn(ProductReturnRequest request) { return service.productReturn(request); }
    @Override public OrderDto paymentSuccess(UUID orderId) { return service.changeState(orderId, ru.yandex.practicum.commerce.api.model.OrderState.PAID); }
    @Override public OrderDto paymentFailed(UUID orderId) { return service.changeState(orderId, ru.yandex.practicum.commerce.api.model.OrderState.PAYMENT_FAILED); }
    @Override public OrderDto delivery(UUID orderId) { return service.changeState(orderId, ru.yandex.practicum.commerce.api.model.OrderState.DELIVERED); }
    @Override public OrderDto deliveryFailed(UUID orderId) { return service.changeState(orderId, ru.yandex.practicum.commerce.api.model.OrderState.DELIVERY_FAILED); }
    @Override public OrderDto complete(UUID orderId) { return service.changeState(orderId, ru.yandex.practicum.commerce.api.model.OrderState.COMPLETED); }
    @Override public OrderDto calculateTotalCost(UUID orderId) { return service.calculateTotalCost(orderId); }
    @Override public OrderDto calculateDeliveryCost(UUID orderId) { return service.calculateDeliveryCost(orderId); }
    @Override public OrderDto assembly(UUID orderId) { return service.assembly(orderId); }
    @Override public OrderDto assemblyFailed(UUID orderId) { return service.changeState(orderId, ru.yandex.practicum.commerce.api.model.OrderState.ASSEMBLY_FAILED); }
}
