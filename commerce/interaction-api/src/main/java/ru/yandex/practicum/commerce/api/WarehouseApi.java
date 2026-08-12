package ru.yandex.practicum.commerce.api;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.api.dto.*;

public interface WarehouseApi {

    @PutMapping("/api/v1/warehouse")
    void addNewProduct(@Valid @RequestBody NewProductInWarehouseRequest request);

    @PostMapping("/api/v1/warehouse/add")
    void addProductQuantity(@Valid @RequestBody AddProductToWarehouseRequest request);

    @PostMapping("/api/v1/warehouse/check")
    BookedProductsDto checkProductQuantity(@Valid @RequestBody ShoppingCartDto cart);

    @GetMapping("/api/v1/warehouse/address")
    AddressDto getWarehouseAddress();
}
