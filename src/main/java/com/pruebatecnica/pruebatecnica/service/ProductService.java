package com.pruebatecnica.pruebatecnica.service;

import com.pruebatecnica.pruebatecnica.model.Product;
import java.util.List;

public interface ProductService {
    Product getProductById(Long productId);

    List<Product> getAllProducts();

    Product saveProduct(Product product);
}