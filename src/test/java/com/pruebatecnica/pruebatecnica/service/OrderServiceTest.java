package com.pruebatecnica.pruebatecnica.service;

import com.pruebatecnica.pruebatecnica.dto.CreateOrderRequest;
import com.pruebatecnica.pruebatecnica.dto.OrderItemRequest;
import com.pruebatecnica.pruebatecnica.model.Order;
import com.pruebatecnica.pruebatecnica.model.Product;
import com.pruebatecnica.pruebatecnica.repository.OrderRepository;
import com.pruebatecnica.pruebatecnica.repository.ProductRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Disabled
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;

    /**
     * NOTA IMPORTANTE: Estos tests están incompletos intencionalmente.
     * Los candidatos deben:
     * 1. Completar los tests faltantes para la lógica del descuento
     * 2. Arreglar los tests que no funcionan debido a la refactorización
     * 3. Agregar más casos de prueba según sea necesario
     */

    @Test
    void testCreateOrderWithoutDiscount_ShouldNotApplyVarietyDiscount() {
        // TODO: Los candidatos deben implementar este test
        // Test para verificar que NO se aplica descuento cuando hay 3 o menos tipos de productos
        fail("Test no implementado - Los candidatos deben completar este test");
    }

    @Test
    void testCreateOrderWithDiscount_ShouldApplyVarietyDiscount() {
        // TODO: Los candidatos deben implementar este test
        // Test para verificar que SÍ se aplica descuento cuando hay más de 3 tipos de productos diferentes
        fail("Test no implementado - Los candidatos deben completar este test");
    }

    @Test
    void testCreateOrderWithSameProductMultipleTimes_ShouldNotApplyDiscount() {
        // TODO: Los candidatos deben implementar este test
        // Ejemplo: 10 manzanas = NO descuento (solo 1 tipo de producto)
        fail("Test no implementado - Los candidatos deben completar este test");
    }

    // Este test básico está roto intencionalmente debido al código monolítico
    @Test
    void testCreateBasicOrder() {
        // Arrange
        Product product1 = new Product("Test Product", BigDecimal.valueOf(10.00), 5);
        product1.setId(1L);
        
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        OrderItemRequest item = new OrderItemRequest(1L, 2);
        CreateOrderRequest request = new CreateOrderRequest("John Doe", "john@test.com", List.of(item));

        // Act & Assert
        // Este test podría fallar después de la refactorización - los candidatos deben arreglarlo
        assertDoesNotThrow(() -> {
            Order result = orderService.createOrder(request);
            assertNotNull(result);
            assertEquals("John Doe", result.getCustomerName());
            assertEquals(BigDecimal.valueOf(20.00), result.getTotalAmount());
        });
    }
}