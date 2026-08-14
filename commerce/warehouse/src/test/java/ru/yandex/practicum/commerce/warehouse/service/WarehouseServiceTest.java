package ru.yandex.practicum.commerce.warehouse.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.commerce.api.dto.AssemblyProductsForOrderRequest;
import ru.yandex.practicum.commerce.api.dto.ShoppingCartDto;
import ru.yandex.practicum.commerce.warehouse.model.OrderBooking;
import ru.yandex.practicum.commerce.warehouse.model.WarehouseProduct;
import ru.yandex.practicum.commerce.warehouse.repository.OrderBookingRepository;
import ru.yandex.practicum.commerce.warehouse.repository.WarehouseProductRepository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WarehouseServiceTest {
    @Mock WarehouseProductRepository productRepository;
    @Mock OrderBookingRepository bookingRepository;

    @Test
    void checkDoesNotDecrementStockAndAssemblyDoes() {
        UUID productId = UUID.randomUUID();
        WarehouseProduct product = product(productId, 10);
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        WarehouseService service = new WarehouseService(productRepository, bookingRepository);

        service.check(new ShoppingCartDto(UUID.randomUUID(), Map.of(productId, 3L)));

        assertThat(product.getQuantity()).isEqualTo(10);
        verify(productRepository, never()).save(any(WarehouseProduct.class));

        UUID orderId = UUID.randomUUID();
        when(bookingRepository.findById(orderId)).thenReturn(Optional.empty());
        when(bookingRepository.save(any(OrderBooking.class))).thenAnswer(invocation -> invocation.getArgument(0));
        service.assemble(new AssemblyProductsForOrderRequest(Map.of(productId, 3L), orderId));

        assertThat(product.getQuantity()).isEqualTo(7);
        verify(productRepository).save(product);
    }

    private WarehouseProduct product(UUID id, long quantity) {
        WarehouseProduct product = new WarehouseProduct();
        product.setProductId(id);
        product.setQuantity(quantity);
        product.setWeight(2);
        product.setWidth(1);
        product.setHeight(2);
        product.setDepth(3);
        return product;
    }
}
