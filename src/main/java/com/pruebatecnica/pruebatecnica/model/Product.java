package com.pruebatecnica.pruebatecnica.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Entidad que representa un producto en el sistema.
 * Contiene información sobre el nombre, precio, stock disponible y descripción.
 */
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock;

    @Column
    private String description;

    /**
     * Constructor por defecto requerido por JPA.
     */
    public Product() {
    }

    /**
     * Constructor para inicializar los campos básicos de un producto.
     *
     * @param name  Nombre del producto.
     * @param price Precio del producto.
     * @param stock Cantidad inicial en inventario.
     */
    public Product(String name, BigDecimal price, Integer stock) {
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}