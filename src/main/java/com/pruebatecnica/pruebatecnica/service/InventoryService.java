package com.pruebatecnica.pruebatecnica.service;

import com.pruebatecnica.pruebatecnica.exception.InsufficientStockException;
import com.pruebatecnica.pruebatecnica.exception.ProductNotFoundException;
import com.pruebatecnica.pruebatecnica.model.Product;
import com.pruebatecnica.pruebatecnica.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio encargado de la gestión de inventario de productos.
 * Proporciona métodos para validar el stock disponible y actualizarlo.
 */
@Service
public class InventoryService {

    @Autowired
    private ProductRepository productRepository;

    /**
     * Obtiene un producto y valida que tenga stock suficiente para la cantidad
     * solicitada.
     *
     * @param productId ID del producto a buscar.
     * @param quantity  Cantidad requerida.
     * @return El producto si existe y tiene stock suficiente.
     * @throws ProductNotFoundException   Si el producto no existe.
     * @throws InsufficientStockException Si el stock disponible es menor a la
     *                                    cantidad solicitada.
     */
    public Product getAndValidateStock(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        if (product.getStock() < quantity) {
            throw new InsufficientStockException(product.getName(), quantity, product.getStock());
        }

        return product;
    }

    /**
     * Deduce una cantidad del stock actual de un producto y guarda los cambios.
     *
     * @param product  El producto cuyo stock será actualizado.
     * @param quantity Cantidad a deducir.
     */
    @Transactional
    public void deductStock(Product product, Integer quantity) {
        product.setStock(product.getStock() - quantity);
        productRepository.save(product);
    }
}
