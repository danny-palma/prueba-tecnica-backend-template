package com.pruebatecnica.pruebatecnica.service;

import com.pruebatecnica.pruebatecnica.model.OrderItem;
import com.pruebatecnica.pruebatecnica.model.Product;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PricingService {
    
    public BigDecimal calculateItemTotal(Product product, Integer quantity) {
        return product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }
    
    public BigDecimal calculateOrderTotal(List<OrderItem> orderItems) {
        return orderItems.stream()
            .map(OrderItem::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public BigDecimal applyDiscount(BigDecimal totalAmount, BigDecimal discountAmount) {
        return totalAmount.subtract(discountAmount);
    }
    
    public BigDecimal calculateDiscountPercentage(BigDecimal totalAmount, BigDecimal discountPercentage) {
        return totalAmount.multiply(discountPercentage);
    }
}