package com.pruebatecnica.pruebatecnica.controller;

import com.pruebatecnica.pruebatecnica.model.Product;
import com.pruebatecnica.pruebatecnica.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de productos.
 * Expone endpoints para listar y consultar información de productos
 * disponibles.
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * Lista todos los productos disponibles en el catálogo.
     *
     * @return ResponseEntity con la lista de productos encontrados.
     */
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    /**
     * Busca un producto específico por su ID.
     *
     * @param id ID del producto a consultar.
     * @return ResponseEntity con el producto o estado 404 (NOT FOUND).
     */
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        try {
            Product product = productService.getProductById(id);
            return ResponseEntity.ok(product);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}