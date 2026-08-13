package ru.yandex.practicum.commerce.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.commerce.api.dto.ChangeProductQuantityRequest;
import ru.yandex.practicum.commerce.api.dto.ShoppingCartDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ShoppingCartApi {

    @GetMapping("/api/v1/shopping-cart")
    ShoppingCartDto getCart(@RequestParam @NotBlank String username);

    @PutMapping("/api/v1/shopping-cart")
    ShoppingCartDto addProducts(@RequestParam @NotBlank String username,
                                @RequestBody Map<UUID, Long> products);

    @PostMapping("/api/v1/shopping-cart/remove")
    ShoppingCartDto removeProducts(@RequestParam @NotBlank String username,
                                   @RequestBody List<UUID> productIds);

    @PostMapping("/api/v1/shopping-cart/change-quantity")
    ShoppingCartDto changeQuantity(@RequestParam @NotBlank String username,
                                   @Valid @RequestBody ChangeProductQuantityRequest request);

    @DeleteMapping("/api/v1/shopping-cart")
    void deactivate(@RequestParam @NotBlank String username);
}
