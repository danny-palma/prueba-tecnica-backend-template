package com.pruebatecnica.pruebatecnica.exception;

/**
 * Excepción lanzada cuando se intenta acceder a un producto que no existe en el
 * sistema.
 */
public class ProductNotFoundException extends RuntimeException {
    private final Long productId;

    /**
     * Constructor de la excepción.
     *
     * @param productId ID del producto que no fue encontrado.
     */
    public ProductNotFoundException(Long productId) {
        super(String.format("Product not found with ID: %d", productId));
        this.productId = productId;
    }

    public Long getProductId() {
        return productId;
    }
}