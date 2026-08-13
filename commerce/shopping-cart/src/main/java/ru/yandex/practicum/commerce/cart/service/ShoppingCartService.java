package ru.yandex.practicum.commerce.cart.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.api.dto.ChangeProductQuantityRequest;
import ru.yandex.practicum.commerce.api.dto.ShoppingCartDto;
import ru.yandex.practicum.commerce.cart.client.WarehouseClient;
import ru.yandex.practicum.commerce.cart.exception.ShoppingCartDeactivatedException;
import ru.yandex.practicum.commerce.cart.exception.ShoppingCartNotFoundException;
import ru.yandex.practicum.commerce.cart.model.ShoppingCart;
import ru.yandex.practicum.commerce.cart.repository.ShoppingCartRepository;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ShoppingCartService {
    private final ShoppingCartRepository repository;
    private final WarehouseClient warehouseClient;

    public ShoppingCartService(ShoppingCartRepository repository, WarehouseClient warehouseClient) {
        this.repository = repository;
        this.warehouseClient = warehouseClient;
    }

    @Transactional
    public ShoppingCartDto getOrCreate(String username) {
        ShoppingCart cart = repository.findTopByUsernameOrderByCreatedAtDesc(username)
                .orElseGet(() -> create(username));
        return toDto(cart);
    }

    @Transactional
    public ShoppingCartDto add(String username, Map<UUID, Long> additions) {
        if (additions == null || additions.isEmpty()) {
            throw new IllegalArgumentException("Products must not be empty");
        }
        ShoppingCart cart = repository.findTopByUsernameOrderByCreatedAtDesc(username)
                .orElseGet(() -> create(username));
        ensureActive(cart);

        Map<UUID, Long> positive = new LinkedHashMap<>();
        additions.forEach((id, qty) -> {
            if (id == null || qty == null || qty <= 0) {
                throw new IllegalArgumentException("Product quantity must be positive");
            }
            positive.put(id, qty);
        });

        // Warehouse performs both the availability check and booking of the requested quantities.
        warehouseClient.checkProductQuantity(new ShoppingCartDto(cart.getShoppingCartId(), positive));

        positive.forEach((id, qty) -> cart.getProducts().merge(id, qty, Math::addExact));
        return toDto(repository.save(cart));
    }

    @Transactional
    public ShoppingCartDto remove(String username, List<UUID> productIds) {
        ShoppingCart cart = find(username);
        ensureActive(cart);
        if (productIds != null) {
            productIds.forEach(cart.getProducts()::remove);
        }
        return toDto(repository.save(cart));
    }

    @Transactional
    public ShoppingCartDto changeQuantity(String username, ChangeProductQuantityRequest request) {
        ShoppingCart cart = find(username);
        ensureActive(cart);
        Long current = cart.getProducts().get(request.productId());
        if (current == null) {
            throw new IllegalArgumentException("Product is not in shopping cart: " + request.productId());
        }
        long requested = request.newQuantity();
        if (requested == 0) {
            cart.getProducts().remove(request.productId());
        } else if (requested > current) {
            long delta = requested - current;
            warehouseClient.checkProductQuantity(new ShoppingCartDto(cart.getShoppingCartId(),
                    Map.of(request.productId(), delta)));
            cart.getProducts().put(request.productId(), requested);
        } else {
            cart.getProducts().put(request.productId(), requested);
        }
        return toDto(repository.save(cart));
    }

    @Transactional
    public void deactivate(String username) {
        ShoppingCart cart = find(username);
        cart.setActive(false);
        repository.save(cart);
    }

    private ShoppingCart create(String username) {
        ShoppingCart cart = new ShoppingCart();
        cart.setShoppingCartId(UUID.randomUUID());
        cart.setUsername(username);
        cart.setActive(true);
        cart.setCreatedAt(Instant.now());
        cart.setProducts(new LinkedHashMap<>());
        return repository.save(cart);
    }

    private ShoppingCart find(String username) {
        return repository.findTopByUsernameOrderByCreatedAtDesc(username)
                .orElseThrow(() -> new ShoppingCartNotFoundException(username));
    }

    private void ensureActive(ShoppingCart cart) {
        if (!cart.isActive()) throw new ShoppingCartDeactivatedException(cart.getUsername());
    }

    private ShoppingCartDto toDto(ShoppingCart cart) {
        return new ShoppingCartDto(cart.getShoppingCartId(), cart.getProducts());
    }
}
