package com.pruebatecnica.pruebatecnica.service.validation;

import com.pruebatecnica.pruebatecnica.exception.InsufficientStockException;
import com.pruebatecnica.pruebatecnica.model.Product;
import org.springframework.stereotype.Component;

/**
 * Validador de stock de productos.
 * Verifica que haya suficiente inventario antes de procesar un pedido.
 */
@Component
public class StockValidator {

    /**
     * Verifica que el producto tenga stock suficiente para la cantidad solicitada.
     * Lanza InsufficientStockException si no hay suficiente stock.
     */
    public void validateStockAvailability(Product product, Integer requestedQuantity) {
        if (product.getStock() < requestedQuantity) {
            throw new InsufficientStockException(
                product.getName(),
                requestedQuantity,
                product.getStock()
            );
        }
    }
}
