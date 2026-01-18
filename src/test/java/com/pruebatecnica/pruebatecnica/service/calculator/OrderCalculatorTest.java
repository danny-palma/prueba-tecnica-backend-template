package com.pruebatecnica.pruebatecnica.service.calculator;

import com.pruebatecnica.pruebatecnica.model.Order;
import com.pruebatecnica.pruebatecnica.model.OrderItem;
import com.pruebatecnica.pruebatecnica.model.Product;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderCalculatorTest {

    private final OrderCalculatorService calculator = new OrderCalculatorService();

    // Helper to build a product
    private Product makeProduct(Long id, BigDecimal price) {
        Product p = new Product("Product " + id, price, 99); // stock cualquiera válido
        p.setId(id);
        return p;
    }

    // Helper to build an item
    private OrderItem makeItem(Product product, int qty) {
        return new OrderItem(product, qty);
    }

    // -------------------------------------------------------------
    // 1. NO aplica descuento (3 productos o menos)
    // -------------------------------------------------------------
    @Test
    void shouldNotApplyDiscount_WhenThreeOrLessProductTypes() {
        // Arrange
        Order order = new Order("Juan", "juan@correo.com");

        Product p1 = makeProduct(1L, BigDecimal.valueOf(10));
        Product p2 = makeProduct(2L, BigDecimal.valueOf(20));
        Product p3 = makeProduct(3L, BigDecimal.valueOf(30));

        order.setItems(List.of(
                makeItem(p1, 1),
                makeItem(p2, 1),
                makeItem(p3, 1)
        ));

        // Act
        calculator.calculateTotals(order);

        // Assert
        // 10 + 20 + 30 = 60 (sin descuento)
        assertEquals(BigDecimal.valueOf(60), order.getTotalAmount());
    }

    // -------------------------------------------------------------
    // 2. SÍ aplica 10% de descuento (> 3 tipos)
    // -------------------------------------------------------------
    @Test
    void shouldApplyDiscount_WhenMoreThanThreeProductTypes() {
        // Arrange
        Order order = new Order("Pedro", "pedro@correo.com");

        Product p1 = makeProduct(1L, BigDecimal.valueOf(10));
        Product p2 = makeProduct(2L, BigDecimal.valueOf(20));
        Product p3 = makeProduct(3L, BigDecimal.valueOf(30));
        Product p4 = makeProduct(4L, BigDecimal.valueOf(40));

        order.setItems(List.of(
                makeItem(p1, 1),
                makeItem(p2, 1),
                makeItem(p3, 1),
                makeItem(p4, 1)
        ));

        // Act
        calculator.calculateTotals(order);

        // Assert
        // Total bruto = 10 + 20 + 30 + 40 = 100
        // Descuento 10% = 10
        // Total final = 90
        assertEquals(0, BigDecimal.valueOf(90).compareTo(order.getTotalAmount()));
    }

    // -------------------------------------------------------------
    // 3. Mismo producto muchas veces → NO aplica descuento
    // -------------------------------------------------------------
    @Test
    void shouldNotApplyDiscount_WhenRepeatedSameProduct() {
        // Arrange
        Order order = new Order("Marcos", "marcos@correo.com");

        Product apple = makeProduct(1L, BigDecimal.valueOf(5));

        order.setItems(List.of(
                makeItem(apple, 10),
                makeItem(apple, 5) // mismo producto → sigue siendo 1 tipo
        ));

        // Act
        calculator.calculateTotals(order);

        // Assert
        // 10*5 + 5*5 = 75 sin descuento
        assertEquals(BigDecimal.valueOf(75), order.getTotalAmount());
    }
}
