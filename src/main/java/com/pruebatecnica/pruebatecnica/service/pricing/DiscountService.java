package com.pruebatecnica.pruebatecnica.service.pricing;

import com.pruebatecnica.pruebatecnica.model.OrderItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Servicio encargado de aplicar descuentos a los pedidos.
 */
@Component
public class DiscountService {

    // Se requieren más de 3 tipos de productos diferentes para el descuento
    private static final int MIN_UNIQUE_PRODUCTS_FOR_VARIETY_DISCOUNT = 3;

    // Porcentaje de descuento por variedad: 10%
    private static final BigDecimal VARIETY_DISCOUNT_PERCENTAGE = BigDecimal.valueOf(0.10);

    /**
     * Aplica los descuentos correspondientes al pedido.
     */
    public BigDecimal applyDiscounts(BigDecimal subtotal, List<OrderItem> items) {
        if (qualifiesForVarietyDiscount(items)) {
            return applyVarietyDiscount(subtotal);
        }
        return subtotal;
    }

    /**
     * Descuento "Variedad": Si el pedido tiene más de 3 TIPOS de productos diferentes,
     * aplica 10% de descuento.
     *
     * Importante: Cuenta tipos de productos, NO cantidad de items.
     * Ejemplo: 10 Manzanas = 1 tipo (NO aplica), 1 Manzana + 1 Pera + 1 Uva + 1 Sandía = 4 tipos (SÍ aplica)
     */
    private boolean qualifiesForVarietyDiscount(List<OrderItem> items) {
        long uniqueProductCount = items.stream()
            .map(item -> item.getProduct().getId())
            .distinct()
            .count();

        return uniqueProductCount > MIN_UNIQUE_PRODUCTS_FOR_VARIETY_DISCOUNT;
    }

    /**
     * Aplica el 10% de descuento al subtotal.
     */
    private BigDecimal applyVarietyDiscount(BigDecimal subtotal) {
        BigDecimal discount = subtotal.multiply(VARIETY_DISCOUNT_PERCENTAGE);
        return subtotal.subtract(discount);
    }
}
