package com.pruebatecnica.pruebatecnica.service;

import com.pruebatecnica.pruebatecnica.model.Product;
import com.pruebatecnica.pruebatecnica.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio encargado de la lógica de negocio relacionada con los productos.
 * Proporciona métodos para consultar y persistir información de productos.
 */
@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    /**
     * Obtiene un producto por su identificador único.
     *
     * @param productId ID del producto a buscar.
     * @return El producto encontrado.
     * @throws RuntimeException Si el producto no es encontrado.
     */
    public Product getProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
    }

    /**
     * Recupera la lista completa de productos registrados en el sistema.
     *
     * @return Lista de todos los productos.
     */
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * Guarda o actualiza un producto en el sistema.
     *
     * @param product El producto a persistir.
     * @return El producto guardado con su ID generado.
     */
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }
}