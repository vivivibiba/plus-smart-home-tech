package ru.yandex.practicum.commerce.store.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.api.ShoppingStoreApi;
import ru.yandex.practicum.commerce.api.dto.ProductDto;
import ru.yandex.practicum.commerce.api.model.ProductCategory;
import ru.yandex.practicum.commerce.api.model.QuantityState;
import ru.yandex.practicum.commerce.store.service.ProductService;

import java.util.UUID;

@RestController
public class ShoppingStoreController implements ShoppingStoreApi {
    private final ProductService service;

    public ShoppingStoreController(ProductService service) {
        this.service = service;
    }

    @Override
    public Page<ProductDto> getProducts(ProductCategory category, Pageable pageable) {
        return service.getProducts(category, pageable);
    }

    @Override
    public ProductDto getProduct(UUID productId) {
        return service.getProduct(productId);
    }

    @Override
    public ProductDto createProduct(ProductDto productDto) {
        return service.create(productDto);
    }

    @Override
    public ProductDto updateProduct(ProductDto productDto) {
        return service.update(productDto);
    }

    @Override
    public boolean removeProduct(UUID productId) {
        return service.remove(productId);
    }

    @Override
    public boolean setQuantityState(UUID productId, QuantityState quantityState) {
        return service.setQuantityState(productId, quantityState);
    }
}
