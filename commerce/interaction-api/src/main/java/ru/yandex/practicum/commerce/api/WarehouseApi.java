package ru.yandex.practicum.commerce.api;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.api.dto.*;

import java.util.Map;
import java.util.UUID;

public interface WarehouseApi {

    @PutMapping("/api/v1/warehouse")
    void addNewProduct(@Valid @RequestBody NewProductInWarehouseRequest request);

    @PostMapping("/api/v1/warehouse/add")
    void addProductQuantity(@Valid @RequestBody AddProductToWarehouseRequest request);

    @PostMapping("/api/v1/warehouse/check")
    BookedProductsDto checkProductQuantity(@Valid @RequestBody ShoppingCartDto cart);

    @PostMapping("/api/v1/warehouse/assembly")
    BookedProductsDto assemblyProductsForOrder(@Valid @RequestBody AssemblyProductsForOrderRequest request);

    @PostMapping("/api/v1/warehouse/shipped")
    void shippedToDelivery(@Valid @RequestBody ShippedToDeliveryRequest request);

    @PostMapping("/api/v1/warehouse/return")
    void acceptReturn(@RequestBody Map<UUID, Long> products);

    @GetMapping("/api/v1/warehouse/address")
    AddressDto getWarehouseAddress();
}
