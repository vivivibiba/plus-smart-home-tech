package ru.yandex.practicum.commerce.store.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.commerce.api.dto.ProductDto;
import ru.yandex.practicum.commerce.api.model.ProductCategory;
import ru.yandex.practicum.commerce.api.model.ProductState;
import ru.yandex.practicum.commerce.api.model.QuantityState;
import ru.yandex.practicum.commerce.store.exception.ProductNotFoundException;
import ru.yandex.practicum.commerce.store.model.Product;
import ru.yandex.practicum.commerce.store.repository.ProductRepository;

import java.util.UUID;

@Service
public class ProductService {
    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> getProducts(ProductCategory category, Pageable pageable) {
        return repository.findAllByProductCategoryAndProductState(
                        category, ProductState.ACTIVE, pageable)
                .map(this::toDto);
    }

    @Transactional(readOnly = true)
    public ProductDto getProduct(UUID id) {
        return toDto(find(id));
    }

    @Transactional
    public ProductDto create(ProductDto dto) {
        Product product = new Product();
        product.setProductId(dto.productId() == null ? UUID.randomUUID() : dto.productId());
        copy(dto, product);
        if (product.getQuantityState() == null) product.setQuantityState(QuantityState.ENDED);
        product.setProductState(ProductState.ACTIVE);
        return toDto(repository.save(product));
    }

    @Transactional
    public ProductDto update(ProductDto dto) {
        if (dto.productId() == null) {
            throw new IllegalArgumentException("productId is required for update");
        }
        Product product = find(dto.productId());
        copy(dto, product);
        if (dto.productState() != null) product.setProductState(dto.productState());
        return toDto(repository.save(product));
    }

    @Transactional
    public boolean remove(UUID id) {
        Product product = find(id);
        product.setProductState(ProductState.DEACTIVATE);
        repository.save(product);
        return true;
    }

    @Transactional
    public boolean setQuantityState(UUID productId, QuantityState quantityState) {
        Product product = find(productId);
        product.setQuantityState(quantityState);
        repository.save(product);
        return true;
    }

    private Product find(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
    }

    private void copy(ProductDto dto, Product product) {
        product.setProductName(dto.productName());
        product.setDescription(dto.description());
        product.setImageSrc(dto.imageSrc());
        product.setPrice(dto.price());
        product.setProductCategory(dto.productCategory());
        if (dto.quantityState() != null) product.setQuantityState(dto.quantityState());
    }

    private ProductDto toDto(Product p) {
        return new ProductDto(p.getProductId(), p.getProductName(), p.getDescription(), p.getImageSrc(),
                p.getQuantityState(), p.getProductState(), p.getPrice(), p.getProductCategory());
    }
}
