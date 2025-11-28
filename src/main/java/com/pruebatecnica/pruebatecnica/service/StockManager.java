package com.pruebatecnica.pruebatecnica.service;

import com.pruebatecnica.pruebatecnica.model.Product;
import com.pruebatecnica.pruebatecnica.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Gestor de inventario que maneja las operaciones de stock.
 * Principio de Responsabilidad Única: Solo se encarga de gestionar el
 * inventario.
 */
@Service
public class StockManager {

    @Autowired
    private ProductRepository productRepository;

    /**
     * Reduce el stock de un producto por la cantidad especificada.
     * 
     * @param product  El producto cuyo stock se reducirá
     * @param quantity La cantidad a reducir
     */
    public void reduceStock(Product product, Integer quantity) {
        int newStock = product.getStock() - quantity;
        product.setStock(newStock);
        productRepository.save(product);
    }

    /**
     * Restaura el stock de un producto (útil para rollbacks).
     * 
     * @param product  El producto cuyo stock se restaurará
     * @param quantity La cantidad a restaurar
     */
    public void restoreStock(Product product, Integer quantity) {
        int newStock = product.getStock() + quantity;
        product.setStock(newStock);
        productRepository.save(product);
    }
}
