package com.pruebatecnica.pruebatecnica.validation;

import com.pruebatecnica.pruebatecnica.exception.InsufficientStockException;
import com.pruebatecnica.pruebatecnica.model.Product;
import org.springframework.stereotype.Component;

@Component
public class StockValidator {
    
    public void validateStockAvailability(Product product, Integer requestedQuantity) {
        if (product.getStock() < requestedQuantity) {
            throw new InsufficientStockException(
                product.getName(), 
                requestedQuantity, 
                product.getStock()
            );
        }
    }
    
    public void reserveStock(Product product, Integer quantity) {
        product.setStock(product.getStock() - quantity);
    }
}