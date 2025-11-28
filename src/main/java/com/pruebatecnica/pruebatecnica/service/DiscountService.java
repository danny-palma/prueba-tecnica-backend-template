package com.pruebatecnica.pruebatecnica.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Servicio de descuentos que aplica reglas de negocio para descuentos.
 * Principio de Responsabilidad Única: Solo se encarga de aplicar descuentos.
 * Principio Abierto/Cerrado: Fácil de extender con nuevas reglas de descuento.
 */
@Service
public class DiscountService {

    private static final int VARIETY_DISCOUNT_THRESHOLD = 3;
    private static final BigDecimal VARIETY_DISCOUNT_PERCENTAGE = BigDecimal.valueOf(0.10);

    /**
     * Aplica el descuento por variedad si corresponde.
     * Regla: Si el pedido contiene más de 3 tipos de productos diferentes,
     * se aplica un 10% de descuento al total.
     * 
     * @param totalAmount      El monto total antes del descuento
     * @param uniqueProductIds Los IDs únicos de productos en la orden
     * @return El monto total después de aplicar el descuento (si corresponde)
     */
    public BigDecimal applyVarietyDiscount(BigDecimal totalAmount, Set<Long> uniqueProductIds) {
        if (qualifiesForVarietyDiscount(uniqueProductIds)) {
            return calculateDiscountedAmount(totalAmount);
        }
        return totalAmount;
    }

    /**
     * Verifica si la orden califica para el descuento por variedad.
     */
    private boolean qualifiesForVarietyDiscount(Set<Long> uniqueProductIds) {
        return uniqueProductIds.size() > VARIETY_DISCOUNT_THRESHOLD;
    }

    /**
     * Calcula el monto con descuento aplicado.
     */
    private BigDecimal calculateDiscountedAmount(BigDecimal totalAmount) {
        BigDecimal discountAmount = totalAmount.multiply(VARIETY_DISCOUNT_PERCENTAGE);
        return totalAmount.subtract(discountAmount);
    }

    /**
     * Calcula el monto del descuento aplicado (útil para reportes).
     */
    public BigDecimal calculateDiscountAmount(BigDecimal totalAmount, Set<Long> uniqueProductIds) {
        if (qualifiesForVarietyDiscount(uniqueProductIds)) {
            return totalAmount.multiply(VARIETY_DISCOUNT_PERCENTAGE);
        }
        return BigDecimal.ZERO;
    }
}
