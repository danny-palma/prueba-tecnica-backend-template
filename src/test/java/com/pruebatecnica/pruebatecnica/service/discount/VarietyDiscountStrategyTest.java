package com.pruebatecnica.pruebatecnica.service.discount;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;

class VarietyDiscountStrategyTest {
    
    private VarietyDiscountStrategy strategy;
    
    @BeforeEach
    void setUp() {
        strategy = new VarietyDiscountStrategy();
    }
    
    @Test
    void calculateDiscount_ThreeUniqueProducts_ShouldNotApplyDiscount() {
        // Arrange
        List<Long> productIds = Arrays.asList(1L, 2L, 3L);
        BigDecimal totalAmount = BigDecimal.valueOf(100.00);
        
        // Act
        BigDecimal discount = strategy.calculateDiscount(productIds, totalAmount);
        
        // Assert
        assertEquals(0, BigDecimal.ZERO.compareTo(discount));
    }
    
    @Test
    void calculateDiscount_FourUniqueProducts_ShouldApplyDiscount() {
        // Arrange
        List<Long> productIds = Arrays.asList(1L, 2L, 3L, 4L);
        BigDecimal totalAmount = BigDecimal.valueOf(100.00);
        
        // Act
        BigDecimal discount = strategy.calculateDiscount(productIds, totalAmount);
        
        // Assert
        assertEquals(0, BigDecimal.valueOf(10.00).compareTo(discount));
    }
    
    @Test
    void calculateDiscount_MoreThanFourUniqueProducts_ShouldApplyDiscount() {
        // Arrange
        List<Long> productIds = Arrays.asList(1L, 2L, 3L, 4L, 5L);
        BigDecimal totalAmount = BigDecimal.valueOf(200.00);
        
        // Act
        BigDecimal discount = strategy.calculateDiscount(productIds, totalAmount);
        
        // Assert
        assertEquals(0, BigDecimal.valueOf(20.00).compareTo(discount));
    }
    
    @Test
    void calculateDiscount_OneProductMultipleTimes_ShouldNotApplyDiscount() {
        // Arrange
        List<Long> productIds = Arrays.asList(1L, 1L, 1L, 1L, 1L);
        BigDecimal totalAmount = BigDecimal.valueOf(50.00);
        
        // Act
        BigDecimal discount = strategy.calculateDiscount(productIds, totalAmount);
        
        // Assert
        assertEquals(0, BigDecimal.ZERO.compareTo(discount));
    }
    
    @Test
    void calculateDiscount_TwoProductsMultipleTimes_ShouldNotApplyDiscount() {
        // Arrange
        List<Long> productIds = Arrays.asList(1L, 1L, 2L, 2L, 2L);
        BigDecimal totalAmount = BigDecimal.valueOf(70.00);
        
        // Act
        BigDecimal discount = strategy.calculateDiscount(productIds, totalAmount);
        
        // Assert
        assertEquals(0, BigDecimal.ZERO.compareTo(discount));
    }
    
    @Test
    void calculateDiscount_EmptyProductList_ShouldNotApplyDiscount() {
        // Arrange
        List<Long> productIds = Arrays.asList();
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        // Act
        BigDecimal discount = strategy.calculateDiscount(productIds, totalAmount);
        
        // Assert
        assertEquals(0, BigDecimal.ZERO.compareTo(discount));
    }
    
    @Test
    void calculateDiscount_ZeroTotalAmount_ShouldReturnZeroDiscount() {
        // Arrange
        List<Long> productIds = Arrays.asList(1L, 2L, 3L, 4L);
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        // Act
        BigDecimal discount = strategy.calculateDiscount(productIds, totalAmount);
        
        // Assert
        assertEquals(0, BigDecimal.ZERO.compareTo(discount));
    }
}