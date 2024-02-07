package io.github.ivankurchenko.blogoteldecoupledmonitoring.controllers;

import io.github.ivankurchenko.blogoteldecoupledmonitoring.domain.Product;
import io.github.ivankurchenko.blogoteldecoupledmonitoring.service.ProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/api/product")
    public Product createProduct(@RequestBody Product data) {
        return productService.createProduct(data);
    }

    @GetMapping("/api/product/{id}")
    public Product getProductById(@PathVariable long id) {
        return productService.getProductById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
    }


    @GetMapping("/api/product")
    public List<Product> searchProducts(@RequestParam(required = false) String query) {
        return productService.searchProducts(query);
    }

    @DeleteMapping("/api/product/{id}")
    public void deleteProductById(@PathVariable long id) {
        productService.deleteProduct(id);
    }
}