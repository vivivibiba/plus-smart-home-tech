package ru.yandex.practicum.commerce.delivery.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.commerce.api.dto.AddressDto;
import ru.yandex.practicum.commerce.api.dto.OrderDto;
import ru.yandex.practicum.commerce.api.model.DeliveryState;
import ru.yandex.practicum.commerce.api.model.OrderState;
import ru.yandex.practicum.commerce.delivery.client.OrderClient;
import ru.yandex.practicum.commerce.delivery.client.WarehouseClient;
import ru.yandex.practicum.commerce.delivery.model.Address;
import ru.yandex.practicum.commerce.delivery.model.Delivery;
import ru.yandex.practicum.commerce.delivery.repository.DeliveryRepository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {
    @Mock DeliveryRepository repository;
    @Mock WarehouseClient warehouseClient;
    @Mock OrderClient orderClient;

    @Test
    void calculatesCostFromTaskExample() {
        UUID orderId = UUID.randomUUID();
        UUID deliveryId = UUID.randomUUID();
        Delivery delivery = new Delivery();
        delivery.setDeliveryId(deliveryId);
        delivery.setOrderId(orderId);
        delivery.setFromAddress(Address.from(new AddressDto("ADDRESS_2", "ADDRESS_2", "ADDRESS_2", "ADDRESS_2", "ADDRESS_2")));
        delivery.setToAddress(Address.from(new AddressDto("Россия", "Москва", "Улица Пролетарская", "31", "1")));
        delivery.setState(DeliveryState.CREATED);
        when(repository.findByOrderId(orderId)).thenReturn(Optional.of(delivery));
        when(repository.save(any(Delivery.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(warehouseClient.getWarehouseAddress())
                .thenReturn(new AddressDto("ADDRESS_2", "ADDRESS_2", "ADDRESS_2", "ADDRESS_2", "ADDRESS_2"));
        DeliveryService service = new DeliveryService(repository, warehouseClient, orderClient);
        OrderDto order = new OrderDto(orderId, UUID.randomUUID(), Map.of(UUID.randomUUID(), 1L),
                null, deliveryId, OrderState.NEW, 10, 10, true,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

        assertThat(service.cost(order)).isEqualByComparingTo("27.60");
    }
}
