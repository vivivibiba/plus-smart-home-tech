package ru.yandex.practicum.commerce.store.controller;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.api.ShoppingStoreApi;
import ru.yandex.practicum.commerce.api.dto.ProductDto;
import ru.yandex.practicum.commerce.api.dto.SetProductQuantityStateRequest;
import ru.yandex.practicum.commerce.api.model.ProductCategory;
import ru.yandex.practicum.commerce.store.service.ProductService;

import java.util.UUID;

@RestController
public class ShoppingStoreController implements ShoppingStoreApi {
    private final ProductService service;

    public ShoppingStoreController(ProductService service) {
        this.service = service;
    }

    @Override
    public Page<ProductDto> getProducts(ProductCategory category, int page, int size) {
        return service.getProducts(category, page, size);
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
    public boolean setQuantityState(SetProductQuantityStateRequest request) {
        return service.setQuantityState(request);
    }
}
