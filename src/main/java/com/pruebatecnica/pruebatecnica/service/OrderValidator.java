package com.pruebatecnica.pruebatecnica.service;

import com.pruebatecnica.pruebatecnica.dto.CreateOrderRequest;
import com.pruebatecnica.pruebatecnica.dto.OrderItemRequest;
import org.springframework.stereotype.Component;

/**
 * Componente encargado de realizar las validaciones de las solicitudes de
 * pedidos.
 * Verifica la presencia y validez de los datos del cliente y de los ítems
 * solicitados.
 */
@Component
public class OrderValidator {

    /**
     * Valida una solicitud de creación de pedido.
     * Comprueba que el nombre, email e ítems no estén vacíos o nulos.
     *
     * @param request La solicitud a validar.
     * @throws IllegalArgumentException Si alguno de los campos requeridos es
     *                                  inválido.
     */
    public void validate(CreateOrderRequest request) {
        if (request.getCustomerName() == null || request.getCustomerName().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer name is required");
        }
        if (request.getCustomerEmail() == null || request.getCustomerEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer email is required");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order items are required");
        }

        for (OrderItemRequest itemRequest : request.getItems()) {
            validateItem(itemRequest);
        }
    }

    /**
     * Valida un ítem individual dentro de la solicitud de pedido.
     * Verifica que el ID del producto esté presente y que la cantidad sea mayor a
     * cero.
     *
     * @param itemRequest El ítem a validar.
     * @throws IllegalArgumentException Si el ítem es inválido.
     */
    private void validateItem(OrderItemRequest itemRequest) {
        if (itemRequest.getProductId() == null) {
            throw new IllegalArgumentException("Product ID is required");
        }
        if (itemRequest.getQuantity() == null || itemRequest.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
    }
}
