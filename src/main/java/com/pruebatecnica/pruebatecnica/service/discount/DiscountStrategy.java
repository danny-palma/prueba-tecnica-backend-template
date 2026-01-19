package com.pruebatecnica.pruebatecnica.service.discount;

import java.math.BigDecimal;
import java.util.List;

public interface DiscountStrategy {
    BigDecimal calculateDiscount(List<Long> productIds, BigDecimal totalAmount);
}