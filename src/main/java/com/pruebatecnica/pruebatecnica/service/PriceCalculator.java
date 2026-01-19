package com.pruebatecnica.pruebatecnica.service;

import com.pruebatecnica.pruebatecnica.model.OrderItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Componente encargado de realizar los cálculos de precios para los pedidos.
 * Implementa la lógica de sumatoria de ítems y la aplicación de descuentos por
 * variedad.
 */
@Component
public class PriceCalculator {

    private static final int VARIETY_DISCOUNT_THRESHOLD = 3;
    private static final BigDecimal VARIETY_DISCOUNT_RATE = BigDecimal.valueOf(0.10);

    /**
     * Calcula el monto total de un pedido a partir de sus ítems.
     * Suma los subtotales de cada ítem y aplica descuentos si corresponde.
     *
     * @param items Lista de ítems del pedido.
     * @return El monto total final.
     */
    public BigDecimal calculateTotal(List<OrderItem> items) {
        BigDecimal subtotal = items.stream()
                .map(this::calculateItemSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return applyVarietyDiscountIfApplicable(subtotal, items);
    }

    /**
     * Calcula el subtotal de un ítem individual (precio unitario * cantidad).
     *
     * @param item El ítem del pedido.
     * @return El subtotal del ítem.
     */
    private BigDecimal calculateItemSubtotal(OrderItem item) {
        return item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
    }

    /**
     * Aplica el descuento por variedad si el pedido contiene más de 3 tipos de
     * productos diferentes.
     * El descuento aplicado es del 10% sobre el total.
     *
     * @param currentTotal El total actual antes del descuento.
     * @param items        Lista de ítems del pedido para contar tipos de productos
     *                     únicos.
     * @return El total final con el descuento aplicado (si corresponde).
     */
    private BigDecimal applyVarietyDiscountIfApplicable(BigDecimal currentTotal, List<OrderItem> items) {
        long uniqueProductCount = items.stream()
                .map(item -> item.getProduct().getId())
                .distinct()
                .count();

        if (uniqueProductCount > VARIETY_DISCOUNT_THRESHOLD) {
            BigDecimal discount = currentTotal.multiply(VARIETY_DISCOUNT_RATE);
            return currentTotal.subtract(discount);
        }

        return currentTotal;
    }
}
