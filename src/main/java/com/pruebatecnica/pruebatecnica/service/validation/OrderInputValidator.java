package com.pruebatecnica.pruebatecnica.service.validation;

import com.pruebatecnica.pruebatecnica.dto.CreateOrderRequest;
import com.pruebatecnica.pruebatecnica.dto.OrderItemRequest;
import org.springframework.stereotype.Component;

/**
 * Validador de datos de entrada para la creación de pedidos.
 * Verifica que todos los campos requeridos estén presentes y sean válidos.
 */
@Component
public class OrderInputValidator {

    /**
     * Valida todos los datos de entrada del pedido.
     * Lanza IllegalArgumentException si algún dato es inválido.
     */
    public void validate(CreateOrderRequest request) {
        validateCustomerName(request.getCustomerName());
        validateCustomerEmail(request.getCustomerEmail());
        validateOrderItems(request);
    }

    /**
     * Verifica que el nombre del cliente no esté vacío.
     */
    private void validateCustomerName(String customerName) {
        if (customerName == null || customerName.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del cliente es requerido");
        }
    }

    /**
     * Verifica que el email del cliente no esté vacío.
     */
    private void validateCustomerEmail(String customerEmail) {
        if (customerEmail == null || customerEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("El email del cliente es requerido");
        }
    }

    /**
     * Verifica que el pedido tenga al menos un item.
     */
    private void validateOrderItems(CreateOrderRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Los items del pedido son requeridos");
        }

        for (OrderItemRequest item : request.getItems()) {
            validateOrderItem(item);
        }
    }

    /**
     * Verifica que cada item tenga un producto válido y cantidad mayor a 0.
     */
    private void validateOrderItem(OrderItemRequest item) {
        if (item.getProductId() == null) {
            throw new IllegalArgumentException("El ID del producto es requerido");
        }
        if (item.getQuantity() == null || item.getQuantity() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        }
    }
}
