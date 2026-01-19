package com.pruebatecnica.pruebatecnica.repository;

import com.pruebatecnica.pruebatecnica.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para la entidad Product.
 * Proporciona métodos estándar para realizar operaciones CRUD sobre la tabla de
 * productos.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}