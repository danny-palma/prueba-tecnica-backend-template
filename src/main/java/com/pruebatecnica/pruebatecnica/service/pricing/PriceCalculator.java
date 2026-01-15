package com.pruebatecnica.pruebatecnica.service.pricing;

import com.pruebatecnica.pruebatecnica.model.OrderItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Calculador de precios para pedidos.
 * Realiza los cálculos de totales por item y subtotal del pedido.
 */
@Component
public class PriceCalculator {

    /**
     * Calcula el total de un item (precio × cantidad).
     */
    public BigDecimal calculateItemTotal(BigDecimal price, Integer quantity) {
        return price.multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * Calcula el subtotal del pedido sumando el total de todos los items.
     */
    public BigDecimal calculateOrderSubtotal(List<OrderItem> items) {
        return items.stream()
            .map(OrderItem::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
