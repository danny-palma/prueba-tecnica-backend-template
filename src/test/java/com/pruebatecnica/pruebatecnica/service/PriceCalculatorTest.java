package com.pruebatecnica.pruebatecnica.service;

import com.pruebatecnica.pruebatecnica.model.OrderItem;
import com.pruebatecnica.pruebatecnica.model.Product;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PriceCalculatorTest {

    private final PriceCalculator priceCalculator = new PriceCalculator();

    @Test
    void testCalculateTotal_WithoutDiscount() {
        // Arrange: 3 different products
        List<OrderItem> items = new ArrayList<>();
        items.add(createOrderItem(1L, 10.0, 1)); // 10
        items.add(createOrderItem(2L, 20.0, 1)); // 20
        items.add(createOrderItem(3L, 30.0, 1)); // 30
        // Total: 60.0

        // Act
        BigDecimal total = priceCalculator.calculateTotal(items);

        // Assert
        assertEquals(new BigDecimal("60.0"), total);
    }

    @Test
    void testCalculateTotal_WithDiscount() {
        // Arrange: 4 different products
        List<OrderItem> items = new ArrayList<>();
        items.add(createOrderItem(1L, 10.0, 1)); // 10
        items.add(createOrderItem(2L, 20.0, 1)); // 20
        items.add(createOrderItem(3L, 30.0, 1)); // 30
        items.add(createOrderItem(4L, 40.0, 1)); // 40
        // Total: 100.0
        // Discount 10%: 10.0
        // Expected: 90.0

        // Act
        BigDecimal total = priceCalculator.calculateTotal(items);

        // Assert
        assertEquals(new BigDecimal("90.00"), total);
    }

    @Test
    void testCalculateTotal_MultipleQuantities_WithoutDiscount() {
        // Arrange: 2 different products, multiple quantities
        List<OrderItem> items = new ArrayList<>();
        items.add(createOrderItem(1L, 10.0, 10)); // 100
        items.add(createOrderItem(2L, 5.0, 2)); // 10
        // Total: 110.0

        // Act
        BigDecimal total = priceCalculator.calculateTotal(items);

        // Assert
        assertEquals(new BigDecimal("110.0"), total);
    }

    private OrderItem createOrderItem(Long productId, Double price, Integer quantity) {
        Product product = new Product("Product " + productId, BigDecimal.valueOf(price), 100);
        product.setId(productId);
        return new OrderItem(product, quantity);
    }
}
