package com.pruebatecnica.pruebatecnica.service.validator;

import com.pruebatecnica.pruebatecnica.exception.personalized.InsufficientStockException;
import com.pruebatecnica.pruebatecnica.model.Product;
import org.springframework.stereotype.Component;

/**
 * Validador de stock que verifica la disponibilidad de productos.
 * Principio de Responsabilidad Única: Solo se encarga de validar stock.
 */
@Component
public class StockValidator {

    /**
     * Valida que haya suficiente stock disponible para la cantidad solicitada.
     * 
     * @param product           El producto a validar
     * @param requestedQuantity La cantidad solicitada
     * @throws InsufficientStockException si no hay suficiente stock
     */
    public void validateStockAvailability(Product product, Integer requestedQuantity) {
        if (product.getStock() < requestedQuantity) {
            throw new InsufficientStockException(
                    product.getName(),
                    requestedQuantity,
                    product.getStock());
        }
    }
}
