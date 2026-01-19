package com.pruebatecnica.pruebatecnica.service;

import com.pruebatecnica.pruebatecnica.dto.CreateOrderRequest;
import com.pruebatecnica.pruebatecnica.dto.OrderItemRequest;
import com.pruebatecnica.pruebatecnica.model.Order;
import com.pruebatecnica.pruebatecnica.model.Product;
import com.pruebatecnica.pruebatecnica.repository.OrderRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
        //fail("Test no implementado - Los candidatos deben completar este test");
        Product product1 = new Product("Manzana", BigDecimal.valueOf(1.50), 100);
        product1.setId(1L);
        Product product2 = new Product("Pera", BigDecimal.valueOf(2.00), 80);
        product2.setId(2L);
        Product product3 = new Product("Uva", BigDecimal.valueOf(3.50), 60);
        product3.setId(3L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product2));
        when(productRepository.findById(3L)).thenReturn(Optional.of(product3));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArguments()[0]);

        OrderItemRequest item1 = new OrderItemRequest(1L, 1);
        OrderItemRequest item2 = new OrderItemRequest(2L, 1);
        OrderItemRequest item3 = new OrderItemRequest(3L, 1);

        CreateOrderRequest request = new CreateOrderRequest(
                "pedro silva",
                "psilva@mail.com",
                List.of(item1, item2, item3)
        );

        Order response = orderService.createOrder(request);

        BigDecimal expectedTotal = BigDecimal.valueOf(7.00);

        assertEquals(expectedTotal, response.getTotalAmount(),"Total Incorrecto"
        );
    }

    @Test
    void testCreateOrderWithDiscount_ShouldApplyVarietyDiscount() {
        // TODO: Los candidatos deben implementar este test
        // Test para verificar que SÍ se aplica descuento cuando hay más de 3 tipos de productos diferentes
        //fail("Test no implementado - Los candidatos deben completar este test");
        Product product1 = new Product("Manzana", BigDecimal.valueOf(1.50), 100);
        product1.setId(1L);
        Product product2 = new Product("Pera", BigDecimal.valueOf(2.00), 80);
        product2.setId(2L);
        Product product3 = new Product("Uva", BigDecimal.valueOf(3.50), 60);
        product3.setId(3L);
        Product product4 = new Product("Sandia", BigDecimal.valueOf(5.00), 25);
        product4.setId(4l);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product2));
        when(productRepository.findById(3L)).thenReturn(Optional.of(product3));
        when(productRepository.findById(4L)).thenReturn(Optional.of(product4));
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArguments()[0]);

        OrderItemRequest item1 = new OrderItemRequest(1L, 1);
        OrderItemRequest item2 = new OrderItemRequest(2L, 1);
        OrderItemRequest item3 = new OrderItemRequest(3L, 1);
        OrderItemRequest item4 = new OrderItemRequest(4L, 1);

        CreateOrderRequest request = new CreateOrderRequest(
                "pedro silva",
                "psilva@mail.com",
                List.of(item1, item2, item3, item4)
        );

        Order response = orderService.createOrder(request);

        BigDecimal total = BigDecimal.valueOf(12.00);
        total = total.subtract(total.multiply(BigDecimal.valueOf(0.10)));
        assertEquals(response.getTotalAmount(), total,"Total Incorrecto");
    }

    @Test
    void testCreateOrderWithSameProductMultipleTimes_ShouldNotApplyDiscount() {
        // TODO: Los candidatos deben implementar este test
        // Ejemplo: 10 manzanas = NO descuento (solo 1 tipo de producto)
        //fail("Test no implementado - Los candidatos deben completar este test");
        Product product1 = new Product("Manzana", BigDecimal.valueOf(1.50), 100);
        product1.setId(1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArguments()[0]);

        OrderItemRequest item = new OrderItemRequest(1L, 10);

        CreateOrderRequest request = new CreateOrderRequest(
                "pedro silva",
                "psilva@mail.com",
                List.of(item)
        );

        Order response = orderService.createOrder(request);

        BigDecimal total = BigDecimal.valueOf(15.00);
        assertEquals(response.getTotalAmount(), total,"Total Incorrecto");
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