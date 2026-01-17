package com.pruebatecnica.pruebatecnica.exception;

public class OrderNotFoundException extends RuntimeException {
    private final Long orderId;

    public OrderNotFoundException(Long orderId) {
        super(String.format("Order not found with ID: %d", orderId));
        this.orderId = orderId;
    }

    public Long getOrderId() {
        return orderId;
    }
}