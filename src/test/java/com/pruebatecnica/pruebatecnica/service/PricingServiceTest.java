package com.pruebatecnica.pruebatecnica.service;

import com.pruebatecnica.pruebatecnica.model.OrderItem;
import com.pruebatecnica.pruebatecnica.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;

class PricingServiceTest {
    
    private PricingService pricingService;
    private Product product;
    
    @BeforeEach
    void setUp() {
        pricingService = new PricingService();
        product = new Product("Test Product", BigDecimal.valueOf(10.00), 10);
        product.setId(1L);
    }
    
    @Test
    void calculateItemTotal_ValidProductAndQuantity_ShouldReturnCorrectTotal() {
        // Act
        BigDecimal total = pricingService.calculateItemTotal(product, 3);
        
        // Assert
        assertEquals(BigDecimal.valueOf(30.00), total);
    }
    
    @Test
    void calculateItemTotal_QuantityOne_ShouldReturnProductPrice() {
        // Act
        BigDecimal total = pricingService.calculateItemTotal(product, 1);
        
        // Assert
        assertEquals(product.getPrice(), total);
    }
    
    @Test
    void calculateOrderTotal_EmptyList_ShouldReturnZero() {
        // Act
        BigDecimal total = pricingService.calculateOrderTotal(Arrays.asList());
        
        // Assert
        assertEquals(BigDecimal.ZERO, total);
    }
    
    @Test
    void calculateOrderTotal_MultipleItems_ShouldReturnCorrectTotal() {
        // Arrange
        Product product1 = new Product("Product 1", BigDecimal.valueOf(10.00), 10);
        Product product2 = new Product("Product 2", BigDecimal.valueOf(20.00), 10);
        
        OrderItem item1 = new OrderItem(product1, 2);
        item1.setTotalPrice(BigDecimal.valueOf(20.00));
        
        OrderItem item2 = new OrderItem(product2, 1);
        item2.setTotalPrice(BigDecimal.valueOf(20.00));
        
        List<OrderItem> items = Arrays.asList(item1, item2);
        
        // Act
        BigDecimal total = pricingService.calculateOrderTotal(items);
        
        // Assert
        assertEquals(BigDecimal.valueOf(40.00), total);
    }
    
    @Test
    void applyDiscount_ValidDiscount_ShouldReturnDiscountedTotal() {
        // Act
        BigDecimal discounted = pricingService.applyDiscount(BigDecimal.valueOf(100.00), BigDecimal.valueOf(10.00));
        
        // Assert
        assertEquals(BigDecimal.valueOf(90.00), discounted);
    }
    
    @Test
    void applyDiscount_ZeroDiscount_ShouldReturnOriginalTotal() {
        // Act
        BigDecimal discounted = pricingService.applyDiscount(BigDecimal.valueOf(100.00), BigDecimal.ZERO);
        
        // Assert
        assertEquals(BigDecimal.valueOf(100.00), discounted);
    }
    
    @Test
    void calculateDiscountPercentage_TenPercent_ShouldCalculateCorrectly() {
        // Act
        BigDecimal discount = pricingService.calculateDiscountPercentage(
            BigDecimal.valueOf(100.00), 
            BigDecimal.valueOf(0.10)
        );
        
        // Assert
        assertEquals(0, BigDecimal.valueOf(10.00).compareTo(discount));
    }
}