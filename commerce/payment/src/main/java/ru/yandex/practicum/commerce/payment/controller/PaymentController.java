package ru.yandex.practicum.commerce.payment.controller;

import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.api.PaymentApi;
import ru.yandex.practicum.commerce.api.dto.OrderDto;
import ru.yandex.practicum.commerce.api.dto.PaymentDto;
import ru.yandex.practicum.commerce.payment.service.PaymentService;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
public class PaymentController implements PaymentApi {
    private final PaymentService service;

    public PaymentController(PaymentService service) { this.service = service; }

    @Override public PaymentDto payment(OrderDto order) { return service.createPayment(order); }
    @Override public BigDecimal getTotalCost(OrderDto order) { return service.totalCost(order); }
    @Override public void paymentSuccess(UUID paymentId) { service.success(paymentId); }
    @Override public BigDecimal productCost(OrderDto order) { return service.productCost(order); }
    @Override public void paymentFailed(UUID paymentId) { service.failed(paymentId); }
}
