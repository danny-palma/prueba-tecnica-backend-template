package com.pruebatecnica.pruebatecnica.exception.personalized;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class InsufficientStockException extends ResponseStatusException {

    private final String productName;
    private final Integer requestedQuantity;
    private final Integer availableStock;

    public InsufficientStockException(String productName, Integer requestedQuantity, Integer availableStock) {
        super(HttpStatus.BAD_REQUEST, String.format("Insufficient stock for product '%s'. Requested: %d, Available: %d",
                productName, requestedQuantity, availableStock));
        this.productName = productName;
        this.requestedQuantity = requestedQuantity;
        this.availableStock = availableStock;
    }

    public String getProductName() {
        return productName;
    }

    public Integer getRequestedQuantity() {
        return requestedQuantity;
    }

    public Integer getAvailableStock() {
        return availableStock;
    }
}