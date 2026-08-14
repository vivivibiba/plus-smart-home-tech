package ru.yandex.practicum.commerce.delivery.model;

import jakarta.persistence.*;
import ru.yandex.practicum.commerce.api.model.DeliveryState;

import java.util.UUID;

@Entity
@Table(name = "deliveries")
public class Delivery {
    @Id
    private UUID deliveryId;
    @Column(nullable = false, unique = true)
    private UUID orderId;
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "country", column = @Column(name = "from_country")),
            @AttributeOverride(name = "city", column = @Column(name = "from_city")),
            @AttributeOverride(name = "street", column = @Column(name = "from_street")),
            @AttributeOverride(name = "house", column = @Column(name = "from_house")),
            @AttributeOverride(name = "flat", column = @Column(name = "from_flat"))
    })
    private Address fromAddress;
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "country", column = @Column(name = "to_country")),
            @AttributeOverride(name = "city", column = @Column(name = "to_city")),
            @AttributeOverride(name = "street", column = @Column(name = "to_street")),
            @AttributeOverride(name = "house", column = @Column(name = "to_house")),
            @AttributeOverride(name = "flat", column = @Column(name = "to_flat"))
    })
    private Address toAddress;
    @Column(nullable = false)
    private double deliveryWeight;
    @Column(nullable = false)
    private double deliveryVolume;
    @Column(nullable = false)
    private boolean fragile;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryState state;
    @Version
    private long version;

    public UUID getDeliveryId() { return deliveryId; }
    public void setDeliveryId(UUID deliveryId) { this.deliveryId = deliveryId; }
    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }
    public Address getFromAddress() { return fromAddress; }
    public void setFromAddress(Address fromAddress) { this.fromAddress = fromAddress; }
    public Address getToAddress() { return toAddress; }
    public void setToAddress(Address toAddress) { this.toAddress = toAddress; }
    public double getDeliveryWeight() { return deliveryWeight; }
    public void setDeliveryWeight(double deliveryWeight) { this.deliveryWeight = deliveryWeight; }
    public double getDeliveryVolume() { return deliveryVolume; }
    public void setDeliveryVolume(double deliveryVolume) { this.deliveryVolume = deliveryVolume; }
    public boolean isFragile() { return fragile; }
    public void setFragile(boolean fragile) { this.fragile = fragile; }
    public DeliveryState getState() { return state; }
    public void setState(DeliveryState state) { this.state = state; }
}
