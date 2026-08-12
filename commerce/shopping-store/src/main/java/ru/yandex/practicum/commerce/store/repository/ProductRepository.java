package ru.yandex.practicum.commerce.store.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.commerce.api.model.ProductCategory;
import ru.yandex.practicum.commerce.api.model.ProductState;
import ru.yandex.practicum.commerce.store.model.Product;

import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    Page<Product> findAllByProductCategoryAndProductState(ProductCategory category, ProductState state, Pageable pageable);
}
