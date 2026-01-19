package com.pruebatecnica.pruebatecnica.repository;

import com.pruebatecnica.pruebatecnica.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para la entidad Order.
 * Proporciona métodos estándar para realizar operaciones CRUD sobre la tabla de
 * pedidos.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
}