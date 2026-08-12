package ru.yandex.practicum.commerce.warehouse.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.api.dto.*;
import ru.yandex.practicum.commerce.warehouse.exception.NotEnoughProductInWarehouseException;
import ru.yandex.practicum.commerce.warehouse.exception.ProductAlreadyExistsInWarehouseException;
import ru.yandex.practicum.commerce.warehouse.exception.WarehouseProductNotFoundException;
import ru.yandex.practicum.commerce.warehouse.model.WarehouseProduct;
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

    public WarehouseService(WarehouseProductRepository repository) {
        this.repository = repository;
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
    public BookedProductsDto book(ShoppingCartDto cart) {
        if (cart == null || cart.products() == null || cart.products().isEmpty()) {
            throw new IllegalArgumentException("Shopping cart is empty");
        }

        List<UUID> missing = new ArrayList<>();
        double weight = 0.0;
        double volume = 0.0;
        boolean fragile = false;
        List<Reservation> reservations = new ArrayList<>();

        for (Map.Entry<UUID, Long> entry : cart.products().entrySet()) {
            long requested = entry.getValue() == null ? 0L : entry.getValue();
            if (requested <= 0) continue;
            WarehouseProduct product = repository.findById(entry.getKey()).orElse(null);
            if (product == null || product.getQuantity() < requested) {
                missing.add(entry.getKey());
                continue;
            }
            reservations.add(new Reservation(product, requested));
            weight += product.getWeight() * requested;
            volume += product.getWidth() * product.getHeight() * product.getDepth() * requested;
            fragile |= product.isFragile();
        }

        if (!missing.isEmpty()) {
            throw new NotEnoughProductInWarehouseException(missing);
        }

        for (Reservation reservation : reservations) {
            WarehouseProduct product = reservation.product();
            product.setQuantity(product.getQuantity() - reservation.quantity());
            repository.save(product);
        }

        return new BookedProductsDto(weight, volume, fragile);
    }

    public AddressDto address() {
        return new AddressDto(CURRENT_ADDRESS, CURRENT_ADDRESS, CURRENT_ADDRESS, CURRENT_ADDRESS, CURRENT_ADDRESS);
    }

    private WarehouseProduct find(UUID id) {
        return repository.findById(id).orElseThrow(() -> new WarehouseProductNotFoundException(id));
    }

    private record Reservation(WarehouseProduct product, long quantity) {}
}
