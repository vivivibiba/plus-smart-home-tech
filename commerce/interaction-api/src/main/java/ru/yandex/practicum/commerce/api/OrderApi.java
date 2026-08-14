package ru.yandex.practicum.commerce.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.api.dto.CreateNewOrderRequest;
import ru.yandex.practicum.commerce.api.dto.OrderDto;
import ru.yandex.practicum.commerce.api.dto.ProductReturnRequest;

import java.util.List;
import java.util.UUID;

public interface OrderApi {
    @GetMapping("/api/v1/order")
    List<OrderDto> getClientOrders(@RequestParam @NotBlank String username);

    @PutMapping("/api/v1/order")
    OrderDto createNewOrder(@Valid @RequestBody CreateNewOrderRequest request);

    @PostMapping("/api/v1/order/return")
    OrderDto productReturn(@Valid @RequestBody ProductReturnRequest request);

    @PostMapping("/api/v1/order/payment")
    OrderDto paymentSuccess(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/payment/failed")
    OrderDto paymentFailed(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/delivery")
    OrderDto delivery(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/delivery/failed")
    OrderDto deliveryFailed(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/completed")
    OrderDto complete(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/calculate/total")
    OrderDto calculateTotalCost(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/calculate/delivery")
    OrderDto calculateDeliveryCost(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/assembly")
    OrderDto assembly(@RequestBody UUID orderId);

    @PostMapping("/api/v1/order/assembly/failed")
    OrderDto assemblyFailed(@RequestBody UUID orderId);
}
