package com.pruebatecnica.pruebatecnica.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Objeto de transferencia de datos (DTO) para representar un ítem dentro de una
 * solicitud de pedido.
 * Contiene el ID del producto y la cantidad deseada.
 */
public class OrderItemRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    /**
     * Constructor por defecto.
     */
    public OrderItemRequest() {
    }

    /**
     * Constructor con campos obligatorios.
     *
     * @param productId ID del producto seleccionado.
     * @param quantity  Cantidad a comprar.
     */
    public OrderItemRequest(Long productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    // Getters and Setters
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}