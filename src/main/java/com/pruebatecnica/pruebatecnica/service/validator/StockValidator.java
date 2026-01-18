package com.pruebatecnica.pruebatecnica.service.validator;

import com.pruebatecnica.pruebatecnica.dto.CreateOrderRequest;
import com.pruebatecnica.pruebatecnica.exception.InsufficientStockException;
import com.pruebatecnica.pruebatecnica.exception.ProductNotFoundException;
import com.pruebatecnica.pruebatecnica.model.Product;
import com.pruebatecnica.pruebatecnica.repository.ProductRepository;
import org.springframework.stereotype.Service;

@Service
public class StockValidator {

    private final ProductRepository productRepository;

    public StockValidator(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public void validateStock(CreateOrderRequest request) {

        request.getItems().forEach(item -> {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException(item.getProductId()));

            if (product.getStock() < item.getQuantity()) {
                throw new InsufficientStockException(
                        product.getName(),
                        item.getQuantity(),
                        product.getStock()
                );
            }
        });
    }
}
