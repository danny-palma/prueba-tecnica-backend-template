package com.pruebatecnica.pruebatecnica.service;

import com.pruebatecnica.pruebatecnica.model.Product;
import com.pruebatecnica.pruebatecnica.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite de Pruebas Unitarias para ProductService.

 * ESTRATEGIA DE TESTING:

 * 1. OPERACIONES DE CONSULTA:
 * - testGetProductById_Success:
 * Verifica la recuperación exitosa de un producto específico mediante su ID.
 * - testGetAllProducts:
 * Valida que el servicio retorne correctamente el catálogo completo de productos.
 *---------------------------------------------------------------------------------

 * 2. INTEGRIDAD DE DATOS:
 * - Se asegura que los objetos recuperados mantengan la consistencia de sus
 * atributos (ID, Nombre, Stock) tras la consulta al repositorio.
 */

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    @DisplayName("Debe encontrar y retornar un producto por su ID")
    void testGetProductById_Success() {
        // Arrange: Se crea el objeto en memoria del test
        Product p = new Product("Test", BigDecimal.TEN, 5);
        p.setId(1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(p));

        // Act
        Product result = productService.getProductById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("Debe retornar la lista completa con 3 productos")
    void testGetAllProducts() {
        // Arrange: Creamos 3 instancias diferentes de productos
        Product p1 = new Product("Laptop", BigDecimal.valueOf(1000.0), 10);
        Product p2 = new Product("Mouse", BigDecimal.valueOf(20.0), 50);
        Product p3 = new Product("Teclado", BigDecimal.valueOf(30.0), 30);

        when(productRepository.findAll()).thenReturn(List.of(p1, p2, p3));

        // Act
        List<Product> result = productService.getAllProducts();

        // Assert
        assertNotNull(result, "La lista no debería ser nula");
        assertFalse(result.isEmpty(), "La lista no debería estar vacía");
        assertEquals(3, result.size(), "La lista debería contener exactamente 3 productos");
    }

}
