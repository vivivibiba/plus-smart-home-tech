package ru.yandex.practicum.commerce.warehouse.controller;

import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.commerce.api.WarehouseApi;
import ru.yandex.practicum.commerce.api.dto.*;
import ru.yandex.practicum.commerce.warehouse.service.WarehouseService;

import java.util.Map;
import java.util.UUID;

@RestController
public class WarehouseController implements WarehouseApi {
    private final WarehouseService service;

    public WarehouseController(WarehouseService service) {
        this.service = service;
    }

    @Override
    public void addNewProduct(NewProductInWarehouseRequest request) {
        service.addNewProduct(request);
    }

    @Override
    public void addProductQuantity(AddProductToWarehouseRequest request) {
        service.addQuantity(request);
    }

    @Override
    public BookedProductsDto checkProductQuantity(ShoppingCartDto cart) {
        return service.check(cart);
    }

    @Override
    public BookedProductsDto assemblyProductsForOrder(AssemblyProductsForOrderRequest request) {
        return service.assemble(request);
    }

    @Override
    public void shippedToDelivery(ShippedToDeliveryRequest request) {
        service.shippedToDelivery(request);
    }

    @Override
    public void acceptReturn(Map<UUID, Long> products) {
        service.acceptReturn(products);
    }

    @Override
    public AddressDto getWarehouseAddress() {
        return service.address();
    }
}
