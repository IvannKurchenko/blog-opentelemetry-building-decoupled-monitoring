package io.github.ivankurchenko.blogoteldecoupledmonitoring.service;

import io.github.ivankurchenko.blogoteldecoupledmonitoring.domain.Product;
import io.github.ivankurchenko.blogoteldecoupledmonitoring.repository.ProductDocument;
import io.github.ivankurchenko.blogoteldecoupledmonitoring.repository.ProductDocumentRepository;
import io.github.ivankurchenko.blogoteldecoupledmonitoring.repository.ProductEntity;
import io.github.ivankurchenko.blogoteldecoupledmonitoring.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final ProductDocumentRepository productDocumentRepository;

    public ProductService(ProductRepository productRepository,
                          ProductDocumentRepository productDocumentRepository) {
        this.productRepository = productRepository;
        this.productDocumentRepository = productDocumentRepository;
    }

    public Product createProduct(Product product) {
        logger.info("Creating product: {}", product);
        var productEntity = convertProductToProductEntity(product);
        var createdProduct = convertProductEntityToProduct(productRepository.save(productEntity));
        logger.info("Created product with id: {}", createdProduct.id());

        productDocumentRepository.save(convertProductToProductDocument(createdProduct));
        return createdProduct;
    }

    public Optional<Product> getProductById(Long id) {
        logger.info("Getting product by id: {}", id);
        return productRepository.findById(id).map(this::convertProductEntityToProduct);
    }

    public List<Product> searchProducts(String query) {
        logger.info("Searching products by query: {}", query);
        var result = productDocumentRepository.findByName(query);
        var ids = result.stream().map(ProductDocument::getId).map(Long::parseLong).toList();

        return productRepository
                .findAllById(ids)
                .stream()
                .map(this::convertProductEntityToProduct)
                .toList();
    }

    public void deleteProduct(Long id) {
        logger.info("Deleting product by id: {}", id);
        productRepository.deleteById(id);
        productDocumentRepository.deleteById(Long.toString(id));
    }

    private Product convertProductEntityToProduct(ProductEntity productEntity) {
        return new Product(
                productEntity.getId(),
                productEntity.getName(),
                productEntity.getDescription(),
                productEntity.getPrice()
        );
    }

    private ProductEntity convertProductToProductEntity(Product product) {
        var productEntity = new ProductEntity();
        productEntity.setId(product.id());
        productEntity.setName(product.name());
        productEntity.setDescription(product.description());
        productEntity.setPrice(product.price());
        return productEntity;
    }

    private ProductDocument convertProductToProductDocument(Product product) {
        var productDocument = new ProductDocument();
        productDocument.setId(Long.toString(product.id()));
        productDocument.setName(product.name());
        productDocument.setDescription(product.description());
        return productDocument;
    }
}

