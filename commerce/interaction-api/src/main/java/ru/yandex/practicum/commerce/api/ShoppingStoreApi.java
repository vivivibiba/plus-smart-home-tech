package ru.yandex.practicum.commerce.api;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.api.dto.ProductDto;
import ru.yandex.practicum.commerce.api.dto.SetProductQuantityStateRequest;
import ru.yandex.practicum.commerce.api.model.ProductCategory;

import java.util.UUID;

public interface ShoppingStoreApi {

    @GetMapping("/api/v1/shopping-store")
    Page<ProductDto> getProducts(@RequestParam ProductCategory category,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size);

    @GetMapping("/api/v1/shopping-store/{productId}")
    ProductDto getProduct(@PathVariable UUID productId);

    @PutMapping("/api/v1/shopping-store")
    ProductDto createProduct(@Valid @RequestBody ProductDto productDto);

    @PostMapping("/api/v1/shopping-store")
    ProductDto updateProduct(@Valid @RequestBody ProductDto productDto);

    @PostMapping("/api/v1/shopping-store/removeProductFromStore")
    boolean removeProduct(@RequestBody UUID productId);

    @PostMapping("/api/v1/shopping-store/quantityState")
    boolean setQuantityState(@Valid @RequestBody SetProductQuantityStateRequest request);
}
