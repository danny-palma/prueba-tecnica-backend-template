package com.pruebatecnica.pruebatecnica.validation;

import com.pruebatecnica.pruebatecnica.dto.CreateOrderRequest;
import com.pruebatecnica.pruebatecnica.dto.OrderItemRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderRequestValidatorTest {
    
    private OrderRequestValidator validator;
    
    @BeforeEach
    void setUp() {
        validator = new OrderRequestValidator();
    }
    
    @Test
    void validate_ValidRequest_ShouldNotThrowException() {
        // Arrange
        OrderItemRequest item = new OrderItemRequest(1L, 2);
        CreateOrderRequest request = new CreateOrderRequest("John Doe", "john@test.com", List.of(item));
        
        // Act & Assert
        assertDoesNotThrow(() -> validator.validate(request));
    }
    
    @Test
    void validate_NullCustomerName_ShouldThrowException() {
        // Arrange
        OrderItemRequest item = new OrderItemRequest(1L, 2);
        CreateOrderRequest request = new CreateOrderRequest(null, "john@test.com", List.of(item));
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validate(request)
        );
        assertEquals("Customer name is required", exception.getMessage());
    }
    
    @Test
    void validate_EmptyCustomerName_ShouldThrowException() {
        // Arrange
        OrderItemRequest item = new OrderItemRequest(1L, 2);
        CreateOrderRequest request = new CreateOrderRequest("   ", "john@test.com", List.of(item));
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validate(request)
        );
        assertEquals("Customer name is required", exception.getMessage());
    }
    
    @Test
    void validate_NullCustomerEmail_ShouldThrowException() {
        // Arrange
        OrderItemRequest item = new OrderItemRequest(1L, 2);
        CreateOrderRequest request = new CreateOrderRequest("John Doe", null, List.of(item));
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validate(request)
        );
        assertEquals("Customer email is required", exception.getMessage());
    }
    
    @Test
    void validate_EmptyCustomerEmail_ShouldThrowException() {
        // Arrange
        OrderItemRequest item = new OrderItemRequest(1L, 2);
        CreateOrderRequest request = new CreateOrderRequest("John Doe", "   ", List.of(item));
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validate(request)
        );
        assertEquals("Customer email is required", exception.getMessage());
    }
    
    @Test
    void validate_NullItems_ShouldThrowException() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest("John Doe", "john@test.com", null);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validate(request)
        );
        assertEquals("Order items are required", exception.getMessage());
    }
    
    @Test
    void validate_EmptyItems_ShouldThrowException() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest("John Doe", "john@test.com", List.of());
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validate(request)
        );
        assertEquals("Order items are required", exception.getMessage());
    }
    
    @Test
    void validate_NullProductId_ShouldThrowException() {
        // Arrange
        OrderItemRequest item = new OrderItemRequest(null, 2);
        CreateOrderRequest request = new CreateOrderRequest("John Doe", "john@test.com", List.of(item));
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validate(request)
        );
        assertEquals("Product ID is required", exception.getMessage());
    }
    
    @Test
    void validate_NullQuantity_ShouldThrowException() {
        // Arrange
        OrderItemRequest item = new OrderItemRequest(1L, null);
        CreateOrderRequest request = new CreateOrderRequest("John Doe", "john@test.com", List.of(item));
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validate(request)
        );
        assertEquals("Quantity must be greater than 0", exception.getMessage());
    }
    
    @Test
    void validate_ZeroQuantity_ShouldThrowException() {
        // Arrange
        OrderItemRequest item = new OrderItemRequest(1L, 0);
        CreateOrderRequest request = new CreateOrderRequest("John Doe", "john@test.com", List.of(item));
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validate(request)
        );
        assertEquals("Quantity must be greater than 0", exception.getMessage());
    }
    
    @Test
    void validate_NegativeQuantity_ShouldThrowException() {
        // Arrange
        OrderItemRequest item = new OrderItemRequest(1L, -1);
        CreateOrderRequest request = new CreateOrderRequest("John Doe", "john@test.com", List.of(item));
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validate(request)
        );
        assertEquals("Quantity must be greater than 0", exception.getMessage());
    }
}