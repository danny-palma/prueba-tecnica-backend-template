package com.pruebatecnica.pruebatecnica.dto;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class DtoTest {

    @Test
    void testCreateOrderRequest() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerName("John");
        request.setCustomerEmail("john@test.com");
        request.setItems(new ArrayList<>());

        assertEquals("John", request.getCustomerName());
        assertEquals("john@test.com", request.getCustomerEmail());
        assertNotNull(request.getItems());
    }

    @Test
    void testOrderItemRequest() {
        OrderItemRequest request = new OrderItemRequest();
        request.setProductId(1L);
        request.setQuantity(5);

        assertEquals(1L, request.getProductId());
        assertEquals(5, request.getQuantity());
    }
}
