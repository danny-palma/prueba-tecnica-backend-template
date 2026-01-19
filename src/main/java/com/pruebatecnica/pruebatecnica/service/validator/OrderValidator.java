package com.pruebatecnica.pruebatecnica.service.validator;

import com.pruebatecnica.pruebatecnica.dto.CreateOrderRequest;
import com.pruebatecnica.pruebatecnica.dto.OrderItemRequest;
import org.springframework.stereotype.Component;

/**
 * Esta clase es la responsable de validar las reglas de 
 * negocio básicas para la creación de órdenes y sus respectivos items.
 *
 * Con el fin de aplicar los principios SOLID, 
 * el principio aplicado es: SRP (Single Responsibility Principle).
 * Esta clase solo se encarga de validaciones, no mezcla lógica de negocio.
 */
@Component
public class OrderValidator {

    /**
     * Se validan primero los campos principales de la orden.
     *
     * @param request objeto con los datos de la orden
     * @throws IllegalArgumentException si algún campo requerido está vacío o nulo
     */
    public void validateRequest(CreateOrderRequest request) {
        if (request.getCustomerName() == null || request.getCustomerName().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer name is required");
        }
        if (request.getCustomerEmail() == null || request.getCustomerEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer email is required");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order items are required");
        }
    }

    /**
     * Luego se validan los campos de cada item de la orden.
     *
     * @param itemRequest objeto con los datos del item
     * @throws IllegalArgumentException si algún campo requerido está vacío o inválido
     */
    public void validateItem(OrderItemRequest itemRequest) {
        if (itemRequest.getProductId() == null) {
            throw new IllegalArgumentException("Product ID is required");
        }
        if (itemRequest.getQuantity() == null || itemRequest.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
    }
}