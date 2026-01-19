package com.pruebatecnica.pruebatecnica.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class ModelTest {

    @Test
    void testProductModel() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Test");
        product.setPrice(BigDecimal.TEN);
        product.setStock(100);
        product.setDescription("Desc");

        assertEquals(1L, product.getId());
        assertEquals("Test", product.getName());
        assertEquals(BigDecimal.TEN, product.getPrice());
        assertEquals(100, product.getStock());
        assertEquals("Desc", product.getDescription());
    }

    @Test
    void testOrderModel() {
        Order order = new Order("John", "john@test.com");
        order.setId(1L);
        order.setTotalAmount(BigDecimal.TEN);
        order.setStatus(OrderStatus.CONFIRMED);
        order.setCreatedAt(LocalDateTime.now());
        order.setItems(new ArrayList<>());

        assertEquals(1L, order.getId());
        assertEquals("John", order.getCustomerName());
        assertEquals("john@test.com", order.getCustomerEmail());
        assertEquals(BigDecimal.TEN, order.getTotalAmount());
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        assertNotNull(order.getCreatedAt());
        assertNotNull(order.getItems());

        order.setCustomerName("Jane");
        order.setCustomerEmail("jane@test.com");
        assertEquals("Jane", order.getCustomerName());
        assertEquals("jane@test.com", order.getCustomerEmail());
    }

    @Test
    void testOrderItemModel() {
        Product product = new Product("P1", BigDecimal.ONE, 10);
        OrderItem item = new OrderItem(product, 5);
        item.setId(1L);
        Order order = new Order();
        item.setOrder(order);
        item.setProduct(product);
        item.setQuantity(10);
        item.setUnitPrice(BigDecimal.ONE);
        item.setTotalPrice(BigDecimal.TEN);

        assertEquals(1L, item.getId());
        assertEquals(order, item.getOrder());
        assertEquals(product, item.getProduct());
        assertEquals(10, item.getQuantity());
        assertEquals(BigDecimal.ONE, item.getUnitPrice());
        assertEquals(BigDecimal.TEN, item.getTotalPrice());
    }
}
