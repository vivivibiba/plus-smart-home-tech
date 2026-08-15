package ru.yandex.practicum.commerce.order.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.api.dto.*;
import ru.yandex.practicum.commerce.api.model.DeliveryState;
import ru.yandex.practicum.commerce.api.model.OrderState;
import ru.yandex.practicum.commerce.order.client.DeliveryClient;
import ru.yandex.practicum.commerce.order.client.PaymentClient;
import ru.yandex.practicum.commerce.order.client.WarehouseClient;
import ru.yandex.practicum.commerce.order.exception.NoOrderFoundException;
import ru.yandex.practicum.commerce.order.exception.NotAuthorizedUserException;
import ru.yandex.practicum.commerce.order.model.Order;
import ru.yandex.practicum.commerce.order.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OrderService {
    private final OrderRepository repository;
    private final WarehouseClient warehouseClient;
    private final DeliveryClient deliveryClient;
    private final PaymentClient paymentClient;

    public OrderService(OrderRepository repository, WarehouseClient warehouseClient,
                        DeliveryClient deliveryClient, PaymentClient paymentClient) {
        this.repository = repository;
        this.warehouseClient = warehouseClient;
        this.deliveryClient = deliveryClient;
        this.paymentClient = paymentClient;
    }

    @Transactional(readOnly = true)
    public List<OrderDto> getClientOrders(String username) {
        if (username == null || username.isBlank()) {
            throw new NotAuthorizedUserException();
        }
        return repository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional
    public OrderDto create(CreateNewOrderRequest request) {
        ShoppingCartDto cart = request.shoppingCart();
        if (cart.products() == null || cart.products().isEmpty()) {
            throw new IllegalArgumentException("Shopping cart is empty");
        }

        BookedProductsDto booked = warehouseClient.checkProductQuantity(cart);
        Order order = new Order();
        order.setOrderId(UUID.randomUUID());
        order.setShoppingCartId(cart.shoppingCartId());
        order.setProducts(cart.products());
        order.setState(OrderState.NEW);
        order.setDeliveryWeight(booked.deliveryWeight());
        order.setDeliveryVolume(booked.deliveryVolume());
        order.setFragile(booked.fragile());
        order.setDeliveryCountry(request.deliveryAddress().country());
        order.setDeliveryCity(request.deliveryAddress().city());
        order.setDeliveryStreet(request.deliveryAddress().street());
        order.setDeliveryHouse(request.deliveryAddress().house());
        order.setDeliveryFlat(request.deliveryAddress().flat());
        order.setProductPrice(BigDecimal.ZERO);
        order.setDeliveryPrice(BigDecimal.ZERO);
        order.setTotalPrice(BigDecimal.ZERO);
        repository.save(order);

        DeliveryDto delivery = deliveryClient.planDelivery(new DeliveryDto(
                null,
                warehouseClient.getWarehouseAddress(),
                request.deliveryAddress(),
                order.getOrderId(),
                DeliveryState.CREATED
        ));
        order.setDeliveryId(delivery.deliveryId());
        return toDto(repository.save(order));
    }

    @Transactional
    public OrderDto calculateDeliveryCost(UUID orderId) {
        Order order = find(orderId);
        deliveryClient.planDelivery(new DeliveryDto(
                order.getDeliveryId(),
                warehouseClient.getWarehouseAddress(),
                new AddressDto(order.getDeliveryCountry(), order.getDeliveryCity(), order.getDeliveryStreet(),
                        order.getDeliveryHouse(), order.getDeliveryFlat()),
                orderId,
                DeliveryState.CREATED
        ));
        order.setDeliveryPrice(deliveryClient.deliveryCost(toDto(order)));
        return toDto(repository.save(order));
    }

    @Transactional
    public OrderDto calculateTotalCost(UUID orderId) {
        Order order = find(orderId);
        OrderDto current = toDto(order);
        order.setProductPrice(paymentClient.productCost(current));
        order.setTotalPrice(paymentClient.getTotalCost(toDto(order)));
        return toDto(repository.save(order));
    }

    @Transactional
    public OrderDto payment(UUID orderId) {
        Order order = find(orderId);
        if (order.getPaymentId() == null) {
            PaymentDto payment = paymentClient.payment(toDto(order));
            order.setPaymentId(payment.paymentId());
            order.setState(OrderState.ON_PAYMENT);
        } else {
            order.setState(OrderState.PAID);
        }
        return toDto(repository.save(order));
    }

    @Transactional
    public OrderDto assembly(UUID orderId) {
        Order order = find(orderId);
        if (order.getState() != OrderState.ASSEMBLED) {
            BookedProductsDto booked = warehouseClient.assemblyProductsForOrder(
                    new AssemblyProductsForOrderRequest(order.getProducts(), order.getOrderId()));
            order.setDeliveryWeight(booked.deliveryWeight());
            order.setDeliveryVolume(booked.deliveryVolume());
            order.setFragile(booked.fragile());
            order.setState(OrderState.ASSEMBLED);
        }
        return toDto(repository.save(order));
    }

    @Transactional
    public OrderDto productReturn(ProductReturnRequest request) {
        Order order = find(request.orderId());
        for (Map.Entry<UUID, Long> entry : request.products().entrySet()) {
            long ordered = order.getProducts().getOrDefault(entry.getKey(), 0L);
            if (entry.getValue() == null || entry.getValue() <= 0 || entry.getValue() > ordered) {
                throw new IllegalArgumentException("Invalid returned quantity for product: " + entry.getKey());
            }
        }
        warehouseClient.acceptReturn(request.products());
        order.setState(OrderState.PRODUCT_RETURNED);
        return toDto(repository.save(order));
    }

    @Transactional
    public OrderDto changeState(UUID orderId, OrderState state) {
        Order order = find(orderId);
        order.setState(state);
        return toDto(repository.save(order));
    }

    private Order find(UUID orderId) {
        return repository.findById(orderId).orElseThrow(() -> new NoOrderFoundException(orderId));
    }

    private OrderDto toDto(Order order) {
        return new OrderDto(order.getOrderId(), order.getShoppingCartId(), order.getProducts(),
                order.getPaymentId(), order.getDeliveryId(), order.getState(), order.getDeliveryWeight(),
                order.getDeliveryVolume(), order.isFragile(), order.getTotalPrice(),
                order.getDeliveryPrice(), order.getProductPrice());
    }
}
