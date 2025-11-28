package com.pruebatecnica.pruebatecnica.service;

import com.pruebatecnica.pruebatecnica.model.Product;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Calculadora de precios para items de órdenes.
 * Principio de Responsabilidad Única: Solo se encarga de cálculos de precios.
 */
@Service
public class PriceCalculator {

    /**
     * Calcula el precio total de un item (precio unitario * cantidad).
     * 
     * @param product  El producto
     * @param quantity La cantidad
     * @return El precio total del item
     */
    public BigDecimal calculateItemPrice(Product product, Integer quantity) {
        return product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }

    /**
     * Calcula el total sumando todos los subtotales de items.
     * 
     * @param itemPrices Lista de precios de items individuales
     * @return El total de la orden
     */
    public BigDecimal calculateOrderTotal(Iterable<BigDecimal> itemPrices) {
        BigDecimal total = BigDecimal.ZERO;
        for (BigDecimal itemPrice : itemPrices) {
            total = total.add(itemPrice);
        }
        return total;
    }
}
