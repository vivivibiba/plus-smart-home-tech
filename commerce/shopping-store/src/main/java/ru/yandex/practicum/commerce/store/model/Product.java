package ru.yandex.practicum.commerce.store.model;

import jakarta.persistence.*;
import ru.yandex.practicum.commerce.api.model.ProductCategory;
import ru.yandex.practicum.commerce.api.model.ProductState;
import ru.yandex.practicum.commerce.api.model.QuantityState;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "products")
public class Product {
    @Id
    private UUID productId;
    @Column(nullable = false)
    private String productName;
    @Column(nullable = false, length = 4000)
    private String description;
    @Column(nullable = false, length = 2000)
    private String imageSrc;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuantityState quantityState;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductState productState;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductCategory productCategory;

    public Product() {}

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImageSrc() { return imageSrc; }
    public void setImageSrc(String imageSrc) { this.imageSrc = imageSrc; }
    public QuantityState getQuantityState() { return quantityState; }
    public void setQuantityState(QuantityState quantityState) { this.quantityState = quantityState; }
    public ProductState getProductState() { return productState; }
    public void setProductState(ProductState productState) { this.productState = productState; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public ProductCategory getProductCategory() { return productCategory; }
    public void setProductCategory(ProductCategory productCategory) { this.productCategory = productCategory; }
}
