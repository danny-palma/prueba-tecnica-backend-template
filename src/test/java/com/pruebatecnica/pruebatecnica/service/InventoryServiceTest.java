package com.pruebatecnica.pruebatecnica.service;

import com.pruebatecnica.pruebatecnica.exception.InsufficientStockException;
import com.pruebatecnica.pruebatecnica.exception.ProductNotFoundException;
import com.pruebatecnica.pruebatecnica.model.Product;
import com.pruebatecnica.pruebatecnica.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    void testGetAndValidateStock_Success() {
        Product product = new Product("Test", BigDecimal.TEN, 10);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Product result = inventoryService.getAndValidateStock(1L, 5);

        assertNotNull(result);
        assertEquals(10, result.getStock());
    }

    @Test
    void testGetAndValidateStock_NotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> inventoryService.getAndValidateStock(1L, 5));
    }

    @Test
    void testGetAndValidateStock_Insufficient() {
        Product product = new Product("Test", BigDecimal.TEN, 3);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThrows(InsufficientStockException.class, () -> inventoryService.getAndValidateStock(1L, 5));
    }

    @Test
    void testDeductStock() {
        Product product = new Product("Test", BigDecimal.TEN, 10);

        inventoryService.deductStock(product, 3);

        assertEquals(7, product.getStock());
        verify(productRepository).save(product);
    }
}
