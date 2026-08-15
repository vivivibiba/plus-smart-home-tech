package ru.yandex.practicum.commerce.delivery.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.api.dto.AddressDto;
import ru.yandex.practicum.commerce.api.dto.DeliveryDto;
import ru.yandex.practicum.commerce.api.dto.OrderDto;
import ru.yandex.practicum.commerce.api.dto.ShippedToDeliveryRequest;
import ru.yandex.practicum.commerce.api.model.DeliveryState;
import ru.yandex.practicum.commerce.delivery.client.OrderClient;
import ru.yandex.practicum.commerce.delivery.client.WarehouseClient;
import ru.yandex.practicum.commerce.delivery.exception.DeliveryNotFoundException;
import ru.yandex.practicum.commerce.delivery.model.Address;
import ru.yandex.practicum.commerce.delivery.model.Delivery;
import ru.yandex.practicum.commerce.delivery.repository.DeliveryRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.UUID;

@Service
public class DeliveryService {
    private static final BigDecimal BASE_COST = new BigDecimal("5.0");
    private static final BigDecimal TWENTY_PERCENT = new BigDecimal("0.20");
    private static final BigDecimal WEIGHT_RATE = new BigDecimal("0.30");
    private static final BigDecimal VOLUME_RATE = new BigDecimal("0.20");

    private final DeliveryRepository repository;
    private final WarehouseClient warehouseClient;
    private final OrderClient orderClient;

    public DeliveryService(DeliveryRepository repository, WarehouseClient warehouseClient,
                           OrderClient orderClient) {
        this.repository = repository;
        this.warehouseClient = warehouseClient;
        this.orderClient = orderClient;
    }

    @Transactional
    public DeliveryDto plan(DeliveryDto dto) {
        Delivery existing = repository.findByOrderId(dto.orderId()).orElse(null);
        if (existing != null) {
            existing.setFromAddress(Address.from(dto.fromAddress()));
            existing.setToAddress(Address.from(dto.toAddress()));
            if (dto.deliveryState() != null) {
                existing.setState(dto.deliveryState());
            }
            repository.save(existing);
            return toDto(existing);
        }
        Delivery delivery = new Delivery();
        delivery.setDeliveryId(dto.deliveryId() == null ? UUID.randomUUID() : dto.deliveryId());
        delivery.setOrderId(dto.orderId());
        delivery.setFromAddress(Address.from(dto.fromAddress()));
        delivery.setToAddress(Address.from(dto.toAddress()));
        delivery.setState(dto.deliveryState() == null ? DeliveryState.CREATED : dto.deliveryState());
        return toDto(repository.save(delivery));
    }

    @Transactional
    public BigDecimal cost(OrderDto order) {
        Delivery delivery = repository.findByOrderId(order.orderId())
                .orElseThrow(() -> new DeliveryNotFoundException(order.deliveryId()));
        AddressDto warehouse = warehouseClient.getWarehouseAddress();
        AddressDto destination = delivery.getToAddress().toDto();

        BigDecimal addressMultiplier = contains(warehouse, "ADDRESS_2")
                ? new BigDecimal("2") : BigDecimal.ONE;
        BigDecimal result = BASE_COST.add(BASE_COST.multiply(addressMultiplier));
        if (order.fragile()) {
            result = result.add(result.multiply(TWENTY_PERCENT));
        }
        result = result.add(BigDecimal.valueOf(order.deliveryWeight()).multiply(WEIGHT_RATE));
        result = result.add(BigDecimal.valueOf(order.deliveryVolume()).multiply(VOLUME_RATE));
        if (!Objects.equals(warehouse.street(), destination.street())) {
            result = result.add(result.multiply(TWENTY_PERCENT));
        }

        delivery.setDeliveryWeight(order.deliveryWeight());
        delivery.setDeliveryVolume(order.deliveryVolume());
        delivery.setFragile(order.fragile());
        repository.save(delivery);
        return result.setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional
    public void picked(UUID deliveryId) {
        Delivery delivery = find(deliveryId);
        if (delivery.getState() == DeliveryState.IN_PROGRESS) {
            return;
        }
        orderClient.assembly(delivery.getOrderId());
        warehouseClient.shippedToDelivery(new ShippedToDeliveryRequest(delivery.getOrderId(), deliveryId));
        delivery.setState(DeliveryState.IN_PROGRESS);
        repository.save(delivery);
    }

    @Transactional
    public void successful(UUID deliveryId) {
        Delivery delivery = find(deliveryId);
        delivery.setState(DeliveryState.DELIVERED);
        repository.save(delivery);
        orderClient.delivery(delivery.getOrderId());
    }

    @Transactional
    public void failed(UUID deliveryId) {
        Delivery delivery = find(deliveryId);
        delivery.setState(DeliveryState.FAILED);
        repository.save(delivery);
        orderClient.deliveryFailed(delivery.getOrderId());
    }

    private Delivery find(UUID deliveryId) {
        return repository.findById(deliveryId).orElseThrow(() -> new DeliveryNotFoundException(deliveryId));
    }

    private DeliveryDto toDto(Delivery delivery) {
        return new DeliveryDto(delivery.getDeliveryId(), delivery.getFromAddress().toDto(),
                delivery.getToAddress().toDto(), delivery.getOrderId(), delivery.getState());
    }

    private boolean contains(AddressDto address, String value) {
        return value.equals(address.country()) || value.equals(address.city())
                || value.equals(address.street()) || value.equals(address.house())
                || value.equals(address.flat());
    }
}
