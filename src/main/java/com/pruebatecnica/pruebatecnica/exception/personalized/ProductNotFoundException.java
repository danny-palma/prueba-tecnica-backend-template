package com.pruebatecnica.pruebatecnica.exception.personalized;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ProductNotFoundException extends ResponseStatusException {
    private final Long productId;

    public ProductNotFoundException(Long productId) {
        super(HttpStatus.BAD_REQUEST, String.format("Product not found with ID: %d", productId));
        this.productId = productId;
    }

    public Long getProductId() {
        return productId;
    }
}