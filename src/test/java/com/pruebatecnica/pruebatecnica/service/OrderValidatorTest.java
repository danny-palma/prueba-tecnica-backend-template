package com.pruebatecnica.pruebatecnica.service;

import com.pruebatecnica.pruebatecnica.dto.CreateOrderRequest;
import com.pruebatecnica.pruebatecnica.dto.OrderItemRequest;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderValidatorTest {

    private final OrderValidator validator = new OrderValidator();

    @Test
    void testValidate_Success() {
        CreateOrderRequest request = new CreateOrderRequest("John Doe", "john@test.com",
                List.of(new OrderItemRequest(1L, 2)));
        assertDoesNotThrow(() -> validator.validate(request));
    }

    @Test
    void testValidate_NameNull() {
        CreateOrderRequest request = new CreateOrderRequest(null, "john@test.com",
                List.of(new OrderItemRequest(1L, 2)));
        assertThrows(IllegalArgumentException.class, () -> validator.validate(request));
    }

    @Test
    void testValidate_NameEmpty() {
        CreateOrderRequest request = new CreateOrderRequest("", "john@test.com",
                List.of(new OrderItemRequest(1L, 2)));
        assertThrows(IllegalArgumentException.class, () -> validator.validate(request));
    }

    @Test
    void testValidate_NameWhitespaceOnly() {
        CreateOrderRequest request = new CreateOrderRequest("   ", "john@test.com",
                List.of(new OrderItemRequest(1L, 2)));
        assertThrows(IllegalArgumentException.class, () -> validator.validate(request));
    }

    @Test
    void testValidate_EmailNull() {
        CreateOrderRequest request = new CreateOrderRequest("John", null,
                List.of(new OrderItemRequest(1L, 2)));
        assertThrows(IllegalArgumentException.class, () -> validator.validate(request));
    }

    @Test
    void testValidate_EmailEmpty() {
        CreateOrderRequest request = new CreateOrderRequest("John", "",
                List.of(new OrderItemRequest(1L, 2)));
        assertThrows(IllegalArgumentException.class, () -> validator.validate(request));
    }

    @Test
    void testValidate_EmailWhitespaceOnly() {
        CreateOrderRequest request = new CreateOrderRequest("John", "  ",
                List.of(new OrderItemRequest(1L, 2)));
        assertThrows(IllegalArgumentException.class, () -> validator.validate(request));
    }

    @Test
    void testValidate_ItemsNull() {
        CreateOrderRequest request = new CreateOrderRequest("John", "john@test.com", null);
        assertThrows(IllegalArgumentException.class, () -> validator.validate(request));
    }

    @Test
    void testValidate_ItemsEmpty() {
        CreateOrderRequest request = new CreateOrderRequest("John", "john@test.com", Collections.emptyList());
        assertThrows(IllegalArgumentException.class, () -> validator.validate(request));
    }

    @Test
    void testValidate_ItemProductIdNull() {
        CreateOrderRequest request = new CreateOrderRequest("John", "john@test.com",
                List.of(new OrderItemRequest(null, 2)));
        assertThrows(IllegalArgumentException.class, () -> validator.validate(request));
    }

    @Test
    void testValidate_ItemQuantityNull() {
        CreateOrderRequest request = new CreateOrderRequest("John", "john@test.com",
                List.of(new OrderItemRequest(1L, null)));
        assertThrows(IllegalArgumentException.class, () -> validator.validate(request));
    }

    @Test
    void testValidate_ItemQuantityZero() {
        CreateOrderRequest request = new CreateOrderRequest("John", "john@test.com",
                List.of(new OrderItemRequest(1L, 0)));
        assertThrows(IllegalArgumentException.class, () -> validator.validate(request));
    }

    @Test
    void testValidate_ItemQuantityNegative() {
        CreateOrderRequest request = new CreateOrderRequest("John", "john@test.com",
                List.of(new OrderItemRequest(1L, -1)));
        assertThrows(IllegalArgumentException.class, () -> validator.validate(request));
    }
}
