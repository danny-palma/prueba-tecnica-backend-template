package com.pruebatecnica.pruebatecnica.model;

/**
 * Enumeración que representa los posibles estados de un pedido en el sistema.
 */
public enum OrderStatus {
    /** El pedido ha sido creado pero aún no se ha procesado completamente. */
    PENDING,
    /** El pedido ha sido validado, el stock descontado y el pago confirmado. */
    CONFIRMED,
    /** El pedido ha sido enviado al cliente. */
    SHIPPED,
    /** El pedido ha sido entregado exitosamente al cliente. */
    DELIVERED,
    /** El pedido ha sido cancelado y no se procesará más. */
    CANCELLED
}