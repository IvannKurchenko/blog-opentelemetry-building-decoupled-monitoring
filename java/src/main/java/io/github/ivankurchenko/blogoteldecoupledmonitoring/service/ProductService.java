package io.github.ivankurchenko.blogoteldecoupledmonitoring.service;

import io.github.ivankurchenko.blogoteldecoupledmonitoring.domain.*;
import io.github.ivankurchenko.blogoteldecoupledmonitoring.repository.ProductEntity;
import io.github.ivankurchenko.blogoteldecoupledmonitoring.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product createProduct(Product product) {
        logger.info("Creating product: {}", product);
        ProductEntity productEntity = convertToProductEntity(product);
        Product createdProduct = convertToProduct(productRepository.save(productEntity));
        logger.info("Created product with id: {}", createdProduct.id());
        return createdProduct;
    }

    public Optional<Product> getProductById(Long id) {
        logger.info("Getting product by id: {}", id);
        return productRepository.findById(id).map(this::convertToProduct);
    }

    public void deleteProduct(Long id) {
        logger.info("Deleting product by id: {}", id);
        productRepository.deleteById(id);
    }

    public Product convertToProduct(ProductEntity productEntity) {
        return new Product(
                productEntity.getId(),
                productEntity.getName(),
                productEntity.getDescription(),
                productEntity.getPrice()
        );
    }

    public ProductEntity convertToProductEntity(Product product) {
        ProductEntity productEntity = new ProductEntity();
        productEntity.setId(product.id());
        productEntity.setName(product.name());
        productEntity.setDescription(product.description());
        productEntity.setPrice(product.price());
        return productEntity;
    }
}

