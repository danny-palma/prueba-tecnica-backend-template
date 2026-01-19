package com.pruebatecnica.pruebatecnica.service.business;

import com.pruebatecnica.pruebatecnica.model.OrderItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * Esta clase es la responsable de encapsular la lógica de cálculo de totales
 * y descuentos en una orden según lo establecido.
 *
 * Con el fin de aplicar los principios SOLID, el
 * principio aplicado es: SRP (Single Responsibility Principle).
 * Esta clase solo se encarga de cálculos, no mezcla validaciones ni persistencia.
 */
@Component
public class OrderCalculator {

    /**
     * Calcula el total de la orden sumando el precio * cantidad de cada item.
     *
     * @param items lista de items de la orden
     * @return total acumulado de la orden
     */
    public BigDecimal calculateTotal(List<OrderItem> items) {
        return items.stream()
                .map(item -> item.getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Aplica la Regla del Descuento "Variedad",
     * cuando el pedido contiene más de 3 tipos de productos diferentes,
     * se aplica un 10% de descuento al total.
     *
     * @param total monto total calculado antes del descuento
     * @param uniqueProductIds conjunto de IDs de productos únicos en la orden
     * @return total ajustado con descuento si aplica
     */
    public BigDecimal applyVarietyDiscount(BigDecimal total, Set<Long> uniqueProductIds) {
        if (uniqueProductIds.size() > 3) {
            BigDecimal discount = total.multiply(BigDecimal.valueOf(0.10));
            return total.subtract(discount);
        }
        return total;
    }
}
