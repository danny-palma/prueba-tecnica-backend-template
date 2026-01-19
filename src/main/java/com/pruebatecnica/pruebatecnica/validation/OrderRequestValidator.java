package com.pruebatecnica.pruebatecnica.validation;

import com.pruebatecnica.pruebatecnica.dto.CreateOrderRequest;
import com.pruebatecnica.pruebatecnica.dto.OrderItemRequest;
import org.springframework.stereotype.Component;

@Component
public class OrderRequestValidator {
    
    public void validate(CreateOrderRequest request) {
        validateCustomerData(request);
        validateOrderItems(request);
    }
    
    private void validateCustomerData(CreateOrderRequest request) {
        if (request.getCustomerName() == null || request.getCustomerName().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer name is required");
        }
        
        if (request.getCustomerEmail() == null || request.getCustomerEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer email is required");
        }
    }
    
    private void validateOrderItems(CreateOrderRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order items are required");
        }
        
        for (OrderItemRequest item : request.getItems()) {
            validateOrderItem(item);
        }
    }
    
    private void validateOrderItem(OrderItemRequest item) {
        if (item.getProductId() == null) {
            throw new IllegalArgumentException("Product ID is required");
        }
        
        if (item.getQuantity() == null || item.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
    }
}