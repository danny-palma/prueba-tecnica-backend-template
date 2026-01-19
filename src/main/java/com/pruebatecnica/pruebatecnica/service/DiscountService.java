package com.pruebatecnica.pruebatecnica.service;

import com.pruebatecnica.pruebatecnica.service.discount.DiscountStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DiscountService {
    
    @Autowired
    private DiscountStrategy discountStrategy;
    
    public BigDecimal calculateDiscount(List<Long> productIds, BigDecimal totalAmount) {
        return discountStrategy.calculateDiscount(productIds, totalAmount);
    }
}