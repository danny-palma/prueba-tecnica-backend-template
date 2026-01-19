package com.pruebatecnica.pruebatecnica.controller;

import com.pruebatecnica.pruebatecnica.dto.CreateOrderRequest;
import com.pruebatecnica.pruebatecnica.model.Order;
import com.pruebatecnica.pruebatecnica.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de pedidos.
 * Expone endpoints para crear y consultar órdenes.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * Crea un nuevo pedido a partir de una solicitud.
     *
     * @param request DTO con los datos del pedido (cliente e ítems).
     * @return ResponseEntity con el pedido creado y estado 201 (CREATED).
     */
    @PostMapping
    public ResponseEntity<Order> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        try {
            Order order = orderService.createOrder(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(order);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    /**
     * Busca un pedido específico por su ID.
     *
     * @param id ID del pedido a consultar.
     * @return ResponseEntity con el pedido o estado 404 (NOT FOUND).
     */
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable Long id) {
        try {
            Order order = orderService.getOrderById(id);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Lista todos los pedidos registrados.
     *
     * @return ResponseEntity con la lista de pedidos.
     */
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }
}