package com.pruebatecnica.pruebatecnica.service;

import com.pruebatecnica.pruebatecnica.model.Product;
import com.pruebatecnica.pruebatecnica.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void testGetProductById_Success() {
        Product product = new Product("Test", BigDecimal.TEN, 10);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Product result = productService.getProductById(1L);

        assertNotNull(result);
        assertEquals("Test", result.getName());
    }

    @Test
    void testGetProductById_NotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> productService.getProductById(1L));
    }

    @Test
    void testGetAllProducts() {
        when(productRepository.findAll()).thenReturn(List.of(new Product("Test", BigDecimal.TEN, 10)));

        List<Product> result = productService.getAllProducts();

        assertEquals(1, result.size());
    }

    @Test
    void testSaveProduct() {
        Product product = new Product("Test", BigDecimal.TEN, 10);
        when(productRepository.save(product)).thenReturn(product);

        Product result = productService.saveProduct(product);

        assertNotNull(result);
        verify(productRepository).save(product);
    }
}
