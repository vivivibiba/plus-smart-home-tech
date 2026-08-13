package ru.yandex.practicum.commerce.cart.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "shopping_carts")
public class ShoppingCart {
    @Id
    private UUID shoppingCartId;
    @Column(nullable = false)
    private String username;
    @Column(nullable = false)
    private boolean active;
    @Column(nullable = false)
    private Instant createdAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "shopping_cart_products", joinColumns = @JoinColumn(name = "shopping_cart_id"))
    @MapKeyColumn(name = "product_id")
    @Column(name = "quantity", nullable = false)
    private Map<UUID, Long> products = new LinkedHashMap<>();

    public ShoppingCart() {}

    public UUID getShoppingCartId() { return shoppingCartId; }
    public void setShoppingCartId(UUID shoppingCartId) { this.shoppingCartId = shoppingCartId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Map<UUID, Long> getProducts() { return products; }
    public void setProducts(Map<UUID, Long> products) { this.products = products; }
}
