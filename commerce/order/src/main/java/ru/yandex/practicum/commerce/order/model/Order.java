package ru.yandex.practicum.commerce.order.model;

import jakarta.persistence.*;
import ru.yandex.practicum.commerce.api.model.OrderState;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    private UUID orderId;
    @Column(nullable = false)
    private UUID shoppingCartId;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "order_products", joinColumns = @JoinColumn(name = "order_id"))
    @MapKeyColumn(name = "product_id")
    @Column(name = "quantity", nullable = false)
    private Map<UUID, Long> products = new LinkedHashMap<>();
    private UUID paymentId;
    private UUID deliveryId;
    private String deliveryCountry;
    private String deliveryCity;
    private String deliveryStreet;
    private String deliveryHouse;
    private String deliveryFlat;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderState state;
    @Column(nullable = false)
    private double deliveryWeight;
    @Column(nullable = false)
    private double deliveryVolume;
    @Column(nullable = false)
    private boolean fragile;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalPrice = BigDecimal.ZERO;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal deliveryPrice = BigDecimal.ZERO;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal productPrice = BigDecimal.ZERO;
    @Version
    private long version;

    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }
    public UUID getShoppingCartId() { return shoppingCartId; }
    public void setShoppingCartId(UUID shoppingCartId) { this.shoppingCartId = shoppingCartId; }
    public Map<UUID, Long> getProducts() { return products; }
    public void setProducts(Map<UUID, Long> products) { this.products = new LinkedHashMap<>(products); }
    public UUID getPaymentId() { return paymentId; }
    public void setPaymentId(UUID paymentId) { this.paymentId = paymentId; }
    public UUID getDeliveryId() { return deliveryId; }
    public void setDeliveryId(UUID deliveryId) { this.deliveryId = deliveryId; }
    public String getDeliveryCountry() { return deliveryCountry; }
    public void setDeliveryCountry(String deliveryCountry) { this.deliveryCountry = deliveryCountry; }
    public String getDeliveryCity() { return deliveryCity; }
    public void setDeliveryCity(String deliveryCity) { this.deliveryCity = deliveryCity; }
    public String getDeliveryStreet() { return deliveryStreet; }
    public void setDeliveryStreet(String deliveryStreet) { this.deliveryStreet = deliveryStreet; }
    public String getDeliveryHouse() { return deliveryHouse; }
    public void setDeliveryHouse(String deliveryHouse) { this.deliveryHouse = deliveryHouse; }
    public String getDeliveryFlat() { return deliveryFlat; }
    public void setDeliveryFlat(String deliveryFlat) { this.deliveryFlat = deliveryFlat; }
    public OrderState getState() { return state; }
    public void setState(OrderState state) { this.state = state; }
    public double getDeliveryWeight() { return deliveryWeight; }
    public void setDeliveryWeight(double deliveryWeight) { this.deliveryWeight = deliveryWeight; }
    public double getDeliveryVolume() { return deliveryVolume; }
    public void setDeliveryVolume(double deliveryVolume) { this.deliveryVolume = deliveryVolume; }
    public boolean isFragile() { return fragile; }
    public void setFragile(boolean fragile) { this.fragile = fragile; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    public BigDecimal getDeliveryPrice() { return deliveryPrice; }
    public void setDeliveryPrice(BigDecimal deliveryPrice) { this.deliveryPrice = deliveryPrice; }
    public BigDecimal getProductPrice() { return productPrice; }
    public void setProductPrice(BigDecimal productPrice) { this.productPrice = productPrice; }
}
