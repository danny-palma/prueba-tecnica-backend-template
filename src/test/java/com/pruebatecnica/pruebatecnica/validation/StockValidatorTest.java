package com.pruebatecnica.pruebatecnica.validation;

import com.pruebatecnica.pruebatecnica.exception.InsufficientStockException;
import com.pruebatecnica.pruebatecnica.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class StockValidatorTest {
    
    private StockValidator stockValidator;
    private Product product;
    
    @BeforeEach
    void setUp() {
        stockValidator = new StockValidator();
        product = new Product("Test Product", BigDecimal.valueOf(10.00), 5);
        product.setId(1L);
    }
    
    @Test
    void validateStockAvailability_SufficientStock_ShouldNotThrowException() {
        // Act & Assert
        assertDoesNotThrow(() -> stockValidator.validateStockAvailability(product, 3));
    }
    
    @Test
    void validateStockAvailability_ExactStock_ShouldNotThrowException() {
        // Act & Assert
        assertDoesNotThrow(() -> stockValidator.validateStockAvailability(product, 5));
    }
    
    @Test
    void validateStockAvailability_InsufficientStock_ShouldThrowException() {
        // Act & Assert
        InsufficientStockException exception = assertThrows(
            InsufficientStockException.class,
            () -> stockValidator.validateStockAvailability(product, 6)
        );
        
        assertEquals("Test Product", exception.getProductName());
        assertEquals(6, exception.getRequestedQuantity());
        assertEquals(5, exception.getAvailableStock());
    }
    
    @Test
    void reserveStock_ValidQuantity_ShouldDecreaseStock() {
        // Act
        stockValidator.reserveStock(product, 3);
        
        // Assert
        assertEquals(2, product.getStock());
    }
    
    @Test
    void reserveStock_AllStock_ShouldSetStockToZero() {
        // Act
        stockValidator.reserveStock(product, 5);
        
        // Assert
        assertEquals(0, product.getStock());
    }
}