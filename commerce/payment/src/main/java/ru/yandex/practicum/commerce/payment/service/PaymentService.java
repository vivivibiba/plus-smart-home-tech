package ru.yandex.practicum.commerce.payment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.api.dto.OrderDto;
import ru.yandex.practicum.commerce.api.dto.PaymentDto;
import ru.yandex.practicum.commerce.api.dto.ProductDto;
import ru.yandex.practicum.commerce.api.model.PaymentState;
import ru.yandex.practicum.commerce.payment.client.OrderClient;
import ru.yandex.practicum.commerce.payment.client.ShoppingStoreClient;
import ru.yandex.practicum.commerce.payment.exception.PaymentNotFoundException;
import ru.yandex.practicum.commerce.payment.model.Payment;
import ru.yandex.practicum.commerce.payment.repository.PaymentRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentService {
    private static final BigDecimal VAT_RATE = new BigDecimal("0.10");

    private final PaymentRepository repository;
    private final ShoppingStoreClient shoppingStoreClient;
    private final OrderClient orderClient;

    public PaymentService(PaymentRepository repository, ShoppingStoreClient shoppingStoreClient,
                          OrderClient orderClient) {
        this.repository = repository;
        this.shoppingStoreClient = shoppingStoreClient;
        this.orderClient = orderClient;
    }

    public BigDecimal productCost(OrderDto order) {
        if (order.products() == null || order.products().isEmpty()) {
            throw new IllegalArgumentException("Order products must not be empty");
        }
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<UUID, Long> entry : order.products().entrySet()) {
            if (entry.getValue() == null || entry.getValue() <= 0) {
                throw new IllegalArgumentException("Product quantity must be positive");
            }
            ProductDto product = shoppingStoreClient.getProduct(entry.getKey());
            total = total.add(product.price().multiply(BigDecimal.valueOf(entry.getValue())));
        }
        return money(total);
    }

    public BigDecimal totalCost(OrderDto order) {
        BigDecimal products = productCost(order);
        BigDecimal delivery = order.deliveryPrice() == null ? BigDecimal.ZERO : order.deliveryPrice();
        return money(products.add(products.multiply(VAT_RATE)).add(delivery));
    }

    @Transactional
    public PaymentDto createPayment(OrderDto order) {
        Payment existing = repository.findByOrderId(order.orderId()).orElse(null);
        if (existing != null) {
            return toDto(existing);
        }

        BigDecimal products = productCost(order);
        BigDecimal fee = money(products.multiply(VAT_RATE));
        BigDecimal delivery = money(order.deliveryPrice() == null ? BigDecimal.ZERO : order.deliveryPrice());
        Payment payment = new Payment();
        payment.setPaymentId(UUID.randomUUID());
        payment.setOrderId(order.orderId());
        payment.setProductTotal(products);
        payment.setFeeTotal(fee);
        payment.setDeliveryTotal(delivery);
        payment.setTotalPayment(money(products.add(fee).add(delivery)));
        payment.setState(PaymentState.PENDING);
        return toDto(repository.save(payment));
    }

    @Transactional
    public void success(UUID paymentId) {
        Payment payment = find(paymentId);
        payment.setState(PaymentState.SUCCESS);
        repository.save(payment);
        orderClient.paymentSuccess(payment.getOrderId());
    }

    @Transactional
    public void failed(UUID paymentId) {
        Payment payment = find(paymentId);
        payment.setState(PaymentState.FAILED);
        repository.save(payment);
        orderClient.paymentFailed(payment.getOrderId());
    }

    private Payment find(UUID paymentId) {
        return repository.findById(paymentId).orElseThrow(() -> new PaymentNotFoundException(paymentId));
    }

    private PaymentDto toDto(Payment payment) {
        return new PaymentDto(payment.getPaymentId(), payment.getTotalPayment(),
                payment.getDeliveryTotal(), payment.getFeeTotal());
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
