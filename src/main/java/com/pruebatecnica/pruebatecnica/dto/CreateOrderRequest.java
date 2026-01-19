package com.pruebatecnica.pruebatecnica.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Objeto de transferencia de datos (DTO) para la creación de un nuevo pedido.
 * Contiene la información necesaria enviada por el cliente.
 */
public class CreateOrderRequest {

    @NotBlank(message = "Customer name is required")
    private String customerName;

    @NotBlank(message = "Customer email is required")
    @Email(message = "Invalid email format")
    private String customerEmail;

    @NotEmpty(message = "Order items are required")
    private List<OrderItemRequest> items;

    /**
     * Constructor por defecto.
     */
    public CreateOrderRequest() {
    }

    /**
     * Constructor con todos los campos obligatorios.
     *
     * @param customerName  Nombre del cliente.
     * @param customerEmail Email de contacto.
     * @param items         Lista de ítems que componen el pedido.
     */
    public CreateOrderRequest(String customerName, String customerEmail, List<OrderItemRequest> items) {
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.items = items;
    }

    // Getters and Setters
    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }
}