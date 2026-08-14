package ru.yandex.practicum.commerce.warehouse.model;

import jakarta.persistence.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "order_bookings")
public class OrderBooking {
    @Id
    private UUID orderId;
    private UUID deliveryId;
    @Column(nullable = false)
    private double deliveryWeight;
    @Column(nullable = false)
    private double deliveryVolume;
    @Column(nullable = false)
    private boolean fragile;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "order_booking_products", joinColumns = @JoinColumn(name = "order_id"))
    @MapKeyColumn(name = "product_id")
    @Column(name = "quantity", nullable = false)
    private Map<UUID, Long> products = new LinkedHashMap<>();
    @Version
    private long version;

    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }
    public UUID getDeliveryId() { return deliveryId; }
    public void setDeliveryId(UUID deliveryId) { this.deliveryId = deliveryId; }
    public double getDeliveryWeight() { return deliveryWeight; }
    public void setDeliveryWeight(double deliveryWeight) { this.deliveryWeight = deliveryWeight; }
    public double getDeliveryVolume() { return deliveryVolume; }
    public void setDeliveryVolume(double deliveryVolume) { this.deliveryVolume = deliveryVolume; }
    public boolean isFragile() { return fragile; }
    public void setFragile(boolean fragile) { this.fragile = fragile; }
    public Map<UUID, Long> getProducts() { return products; }
    public void setProducts(Map<UUID, Long> products) { this.products = new LinkedHashMap<>(products); }
}
