package ru.yandex.practicum.commerce.warehouse.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.api.dto.*;
import ru.yandex.practicum.commerce.warehouse.exception.NotEnoughProductInWarehouseException;
import ru.yandex.practicum.commerce.warehouse.exception.ProductAlreadyExistsInWarehouseException;
import ru.yandex.practicum.commerce.warehouse.exception.WarehouseProductNotFoundException;
import ru.yandex.practicum.commerce.warehouse.model.WarehouseProduct;
import ru.yandex.practicum.commerce.warehouse.model.OrderBooking;
import ru.yandex.practicum.commerce.warehouse.repository.OrderBookingRepository;
import ru.yandex.practicum.commerce.warehouse.repository.WarehouseProductRepository;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class WarehouseService {
    private static final String[] ADDRESSES = {"ADDRESS_1", "ADDRESS_2"};
    private static final String CURRENT_ADDRESS = ADDRESSES[new SecureRandom().nextInt(ADDRESSES.length)];

    private final WarehouseProductRepository repository;
    private final OrderBookingRepository orderBookingRepository;

    public WarehouseService(WarehouseProductRepository repository,
                            OrderBookingRepository orderBookingRepository) {
        this.repository = repository;
        this.orderBookingRepository = orderBookingRepository;
    }

    @Transactional
    public void addNewProduct(NewProductInWarehouseRequest request) {
        if (repository.existsById(request.productId())) {
            throw new ProductAlreadyExistsInWarehouseException(request.productId());
        }
        WarehouseProduct product = new WarehouseProduct();
        product.setProductId(request.productId());
        product.setFragile(request.fragile());
        product.setWidth(request.dimension().width());
        product.setHeight(request.dimension().height());
        product.setDepth(request.dimension().depth());
        product.setWeight(request.weight());
        product.setQuantity(0L);
        repository.save(product);
    }

    @Transactional
    public void addQuantity(AddProductToWarehouseRequest request) {
        WarehouseProduct product = find(request.productId());
        product.setQuantity(Math.addExact(product.getQuantity(), request.quantity()));
        repository.save(product);
    }

    @Transactional
    public BookedProductsDto check(ShoppingCartDto cart) {
        return inspect(cart.products());
    }

    @Transactional
    public BookedProductsDto assemble(AssemblyProductsForOrderRequest request) {
        OrderBooking existing = orderBookingRepository.findById(request.orderId()).orElse(null);
        if (existing != null) {
            return new BookedProductsDto(existing.getDeliveryWeight(), existing.getDeliveryVolume(), existing.isFragile());
        }

        BookedProductsDto booked = inspect(request.products());
        for (Map.Entry<UUID, Long> entry : request.products().entrySet()) {
            long requested = entry.getValue() == null ? 0L : entry.getValue();
            if (requested <= 0) {
                continue;
            }
            WarehouseProduct product = find(entry.getKey());
            product.setQuantity(product.getQuantity() - requested);
            repository.save(product);
        }

        OrderBooking booking = new OrderBooking();
        booking.setOrderId(request.orderId());
        booking.setProducts(request.products());
        booking.setDeliveryWeight(booked.deliveryWeight());
        booking.setDeliveryVolume(booked.deliveryVolume());
        booking.setFragile(booked.fragile());
        orderBookingRepository.save(booking);
        return booked;
    }

    @Transactional
    public void shippedToDelivery(ShippedToDeliveryRequest request) {
        OrderBooking booking = orderBookingRepository.findById(request.orderId())
                .orElseThrow(() -> new IllegalArgumentException("Order booking not found: " + request.orderId()));
        booking.setDeliveryId(request.deliveryId());
        orderBookingRepository.save(booking);
    }

    @Transactional
    public void acceptReturn(Map<UUID, Long> products) {
        if (products == null || products.isEmpty()) {
            throw new IllegalArgumentException("Returned products must not be empty");
        }
        for (Map.Entry<UUID, Long> entry : products.entrySet()) {
            long returned = entry.getValue() == null ? 0L : entry.getValue();
            if (returned <= 0) {
                throw new IllegalArgumentException("Returned quantity must be positive");
            }
            WarehouseProduct product = find(entry.getKey());
            product.setQuantity(Math.addExact(product.getQuantity(), returned));
            repository.save(product);
        }
    }

    private BookedProductsDto inspect(Map<UUID, Long> products) {
        if (products == null || products.isEmpty()) {
            throw new IllegalArgumentException("Shopping cart is empty");
        }

        List<UUID> missing = new ArrayList<>();
        double weight = 0.0;
        double volume = 0.0;
        boolean fragile = false;

        for (Map.Entry<UUID, Long> entry : products.entrySet()) {
            long requested = entry.getValue() == null ? 0L : entry.getValue();
            if (requested <= 0) continue;
            WarehouseProduct product = repository.findById(entry.getKey()).orElse(null);
            if (product == null || product.getQuantity() < requested) {
                missing.add(entry.getKey());
                continue;
            }
            weight += product.getWeight() * requested;
            volume += product.getWidth() * product.getHeight() * product.getDepth() * requested;
            fragile |= product.isFragile();
        }

        if (!missing.isEmpty()) {
            throw new NotEnoughProductInWarehouseException(missing);
        }

        return new BookedProductsDto(weight, volume, fragile);
    }

    public AddressDto address() {
        return new AddressDto(CURRENT_ADDRESS, CURRENT_ADDRESS, CURRENT_ADDRESS, CURRENT_ADDRESS, CURRENT_ADDRESS);
    }

    private WarehouseProduct find(UUID id) {
        return repository.findById(id).orElseThrow(() -> new WarehouseProductNotFoundException(id));
    }
}
