package ru.yandex.practicum.commerce.payment.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.commerce.api.dto.OrderDto;
import ru.yandex.practicum.commerce.api.dto.ProductDto;
import ru.yandex.practicum.commerce.api.model.OrderState;
import ru.yandex.practicum.commerce.api.model.ProductCategory;
import ru.yandex.practicum.commerce.api.model.ProductState;
import ru.yandex.practicum.commerce.api.model.QuantityState;
import ru.yandex.practicum.commerce.payment.client.OrderClient;
import ru.yandex.practicum.commerce.payment.client.ShoppingStoreClient;
import ru.yandex.practicum.commerce.payment.repository.PaymentRepository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
    @Mock PaymentRepository repository;
    @Mock ShoppingStoreClient shoppingStoreClient;
    @Mock OrderClient orderClient;

    @Test
    void calculatesProductsVatAndDelivery() {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        when(shoppingStoreClient.getProduct(first)).thenReturn(product(first, "100.00"));
        when(shoppingStoreClient.getProduct(second)).thenReturn(product(second, "50.00"));
        PaymentService service = new PaymentService(repository, shoppingStoreClient, orderClient);
        OrderDto order = new OrderDto(UUID.randomUUID(), UUID.randomUUID(), Map.of(first, 2L, second, 1L),
                null, null, OrderState.NEW, 0, 0, false, BigDecimal.ZERO,
                new BigDecimal("50.00"), BigDecimal.ZERO);

        assertThat(service.productCost(order)).isEqualByComparingTo("250.00");
        assertThat(service.totalCost(order)).isEqualByComparingTo("325.00");
    }

    private ProductDto product(UUID id, String price) {
        return new ProductDto(id, "name", "description", "image", QuantityState.ENOUGH,
                ProductState.ACTIVE, new BigDecimal(price), ProductCategory.LIGHTING);
    }
}
