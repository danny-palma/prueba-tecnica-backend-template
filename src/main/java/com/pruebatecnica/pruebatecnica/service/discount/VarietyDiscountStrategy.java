package com.pruebatecnica.pruebatecnica.service.discount;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class VarietyDiscountStrategy implements DiscountStrategy {
    
    private static final BigDecimal VARIETY_DISCOUNT_RATE = BigDecimal.valueOf(0.10);
    private static final int MIN_UNIQUE_PRODUCTS_FOR_DISCOUNT = 4;
    
    @Override
    public BigDecimal calculateDiscount(List<Long> productIds, BigDecimal totalAmount) {
        if (hasVarietyDiscount(productIds)) {
            return totalAmount.multiply(VARIETY_DISCOUNT_RATE);
        }
        return BigDecimal.ZERO;
    }
    
    private boolean hasVarietyDiscount(List<Long> productIds) {
        Set<Long> uniqueProductIds = new HashSet<>(productIds);
        return uniqueProductIds.size() > MIN_UNIQUE_PRODUCTS_FOR_DISCOUNT - 1;
    }
}