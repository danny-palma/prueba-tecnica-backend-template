package com.pruebatecnica.pruebatecnica.exception;

/**
 * Excepción lanzada cuando se intenta realizar un pedido de un producto
 * que no tiene suficiente stock disponible en el inventario.
 */
public class InsufficientStockException extends RuntimeException {
    private final String productName;
    private final Integer requestedQuantity;
    private final Integer availableStock;

    /**
     * Constructor de la excepción con detalles sobre el producto y las cantidades.
     *
     * @param productName       Nombre del producto con stock insuficiente.
     * @param requestedQuantity Cantidad que se intentó pedir.
     * @param availableStock    Cantidad disponible actualmente en inventario.
     */
    public InsufficientStockException(String productName, Integer requestedQuantity, Integer availableStock) {
        super(String.format("Insufficient stock for product '%s'. Requested: %d, Available: %d",
                productName, requestedQuantity, availableStock));
        this.productName = productName;
        this.requestedQuantity = requestedQuantity;
        this.availableStock = availableStock;
    }

    public String getProductName() {
        return productName;
    }

    public Integer getRequestedQuantity() {
        return requestedQuantity;
    }

    public Integer getAvailableStock() {
        return availableStock;
    }
}