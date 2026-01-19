package com.pruebatecnica.pruebatecnica.service;

import com.pruebatecnica.pruebatecnica.dto.CreateOrderRequest;
import com.pruebatecnica.pruebatecnica.dto.OrderItemRequest;
import com.pruebatecnica.pruebatecnica.model.Order;
import com.pruebatecnica.pruebatecnica.model.Product;
import com.pruebatecnica.pruebatecnica.repository.OrderRepository;
import com.pruebatecnica.pruebatecnica.repository.ProductRepository;
import com.pruebatecnica.pruebatecnica.service.business.OrderCalculator;
import com.pruebatecnica.pruebatecnica.service.validator.OrderValidator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock 
    private OrderValidator orderValidator;
    
    @Mock 
    private OrderCalculator orderCalculator;

    @Mock
    private ProductService productService;

    @InjectMocks
    private OrderService orderService;

    /**
     * NOTA IMPORTANTE: Estos tests están incompletos intencionalmente.
     * Los candidatos deben:
     * 1. Completar los tests faltantes para la lógica del descuento
     * 2. Arreglar los tests que no funcionan debido a la refactorización
     * 3. Agregar más casos de prueba según sea necesario
     * 
     * RESPUESTA: Se implementaron los test sugeridos y se ajusta el testCreateBasicOrder de acuerdo a la refactorización
     */

    private Product product(long id, String name, double price, int stock) {
        Product p = new Product(name, BigDecimal.valueOf(price), stock);
        p.setId(id);
        return p;
    }

    private OrderItemRequest item(long productId, int qty) {
        return new OrderItemRequest(productId, qty);
    }

    @Test
    void testCreateOrderWithoutDiscount_ShouldNotApplyVarietyDiscount() {
        // Test para verificar que NO se aplica descuento cuando hay 3 o menos tipos de productos
        // Arrange: 3 tipos de productos → NO aplica descuento
        Product p1 = product(1L, "A", 10.0, 10);
        Product p2 = product(2L, "B", 20.0, 10);
        Product p3 = product(3L, "C", 30.0, 10);

        when(productService.validateAndUpdateStock(eq(1L), anyInt())).thenReturn(p1);
        when(productService.validateAndUpdateStock(eq(2L), anyInt())).thenReturn(p2);
        when(productService.validateAndUpdateStock(eq(3L), anyInt())).thenReturn(p3);
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        // El validator no lanza excepciones
        doNothing().when(orderValidator).validateRequest(any(CreateOrderRequest.class));
        doNothing().when(orderValidator).validateItem(any(OrderItemRequest.class));

        // Total sin descuento: 10*1 + 20*1 + 30*1 = 60
        // Como NO aplica descuento, el calculator debe devolver el mismo total
        when(orderCalculator.calculateTotal(anyList())).thenReturn(BigDecimal.valueOf(60));
        when(orderCalculator.applyVarietyDiscount(eq(BigDecimal.valueOf(60)), anySet()))
                .thenReturn(BigDecimal.valueOf(60));

        CreateOrderRequest request = new CreateOrderRequest(
                "Claudia", "claudia@test.com",
                List.of(item(1L, 1), item(2L, 1), item(3L, 1))
        );

        // Act
        Order result = orderService.createOrder(request);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(60), result.getTotalAmount());
        // Verifica que el calculator fue llamado con 3 IDs únicos
        ArgumentCaptor<Set<Long>> idsCaptor = ArgumentCaptor.forClass(Set.class);
        verify(orderCalculator).applyVarietyDiscount(eq(BigDecimal.valueOf(60)), idsCaptor.capture());
        assertEquals(3, idsCaptor.getValue().size());
    }

    @Test
    void testCreateOrderWithDiscount_ShouldApplyVarietyDiscount() {
        // Test para verificar que SÍ se aplica descuento cuando hay más de 3 tipos de productos diferentes
        // Arrange: 4 tipos de productos → SÍ aplica 10%
        Product p1 = product(1L, "A", 10.0, 10);
        Product p2 = product(2L, "B", 20.0, 10);
        Product p3 = product(3L, "C", 30.0, 10);
        Product p4 = product(4L, "D", 40.0, 10);

        when(productService.validateAndUpdateStock(eq(1L), anyInt())).thenReturn(p1);
        when(productService.validateAndUpdateStock(eq(2L), anyInt())).thenReturn(p2);
        when(productService.validateAndUpdateStock(eq(3L), anyInt())).thenReturn(p3);
        when(productService.validateAndUpdateStock(eq(4L), anyInt())).thenReturn(p4);
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        doNothing().when(orderValidator).validateRequest(any(CreateOrderRequest.class));
        doNothing().when(orderValidator).validateItem(any(OrderItemRequest.class));

        // Total base: 10*1 + 20*1 + 30*1 + 40*1 = 100
        when(orderCalculator.calculateTotal(anyList())).thenReturn(BigDecimal.valueOf(100));
        // Con 4 IDs únicos, el calculator debe devolver 90 (10% descuento)
        when(orderCalculator.applyVarietyDiscount(eq(BigDecimal.valueOf(100)), anySet()))
                .thenReturn(BigDecimal.valueOf(90));

        CreateOrderRequest request = new CreateOrderRequest(
                "Patricia", "patricia@test.com",
                List.of(item(1L, 1), item(2L, 1), item(3L, 1), item(4L, 1))
        );

        // Act
        Order result = orderService.createOrder(request);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(90), result.getTotalAmount());
        // Verifica que el calculator fue llamado con 4 IDs únicos
        ArgumentCaptor<Set<Long>> idsCaptor = ArgumentCaptor.forClass(Set.class);
        verify(orderCalculator).applyVarietyDiscount(eq(BigDecimal.valueOf(100)), idsCaptor.capture());
        assertEquals(4, idsCaptor.getValue().size());
    }

    @Test
    void testCreateOrderWithSameProductMultipleTimes_ShouldNotApplyDiscount() {
        // Ejemplo: 10 manzanas = NO descuento (solo 1 tipo de producto)
        // Arrange: mismo producto repetido → solo 1 tipo → NO descuento
        Product p1 = product(1L, "A", 10.0, 100);

        when(productService.validateAndUpdateStock(eq(1L), anyInt())).thenReturn(p1);
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        doNothing().when(orderValidator).validateRequest(any(CreateOrderRequest.class));
        doNothing().when(orderValidator).validateItem(any(OrderItemRequest.class));

        // Total base: 10 * 10 = 100
        when(orderCalculator.calculateTotal(anyList())).thenReturn(BigDecimal.valueOf(100));
        // Con 1 ID único, el calculator debe devolver 100 (sin descuento)
        when(orderCalculator.applyVarietyDiscount(eq(BigDecimal.valueOf(100)), anySet()))
                .thenReturn(BigDecimal.valueOf(100));

        CreateOrderRequest request = new CreateOrderRequest(
                "Juan", "juan@test.com",
                List.of(item(1L, 5), item(1L, 5)) // mismo producto en dos items
        );

        // Act
        Order result = orderService.createOrder(request);

        // Assert
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(100), result.getTotalAmount());
        // Verifica que el calculator fue llamado con 1 ID único
        ArgumentCaptor<Set<Long>> idsCaptor = ArgumentCaptor.forClass(Set.class);
        verify(orderCalculator).applyVarietyDiscount(eq(BigDecimal.valueOf(100)), idsCaptor.capture());
        assertEquals(1, idsCaptor.getValue().size());
    }

    // Este test básico está roto intencionalmente debido al código monolítico
    /*@Test
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
    }*/

    /* Este es el test ajustado tomando en cuenta la refactorización */
    @Test
    void testCreateBasicOrder() {
        // Arrange
        Product product1 = product(1L, "Test Product", 10.00, 5);

        when(productService.validateAndUpdateStock(eq(1L), anyInt())).thenReturn(product1);
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        // El validator no falla
        doNothing().when(orderValidator).validateRequest(any(CreateOrderRequest.class));
        doNothing().when(orderValidator).validateItem(any(OrderItemRequest.class));

        // Total: 10 * 2 = 20; sin descuento
        when(orderCalculator.calculateTotal(anyList())).thenReturn(BigDecimal.valueOf(20.00));
        when(orderCalculator.applyVarietyDiscount(eq(BigDecimal.valueOf(20.00)), anySet()))
                .thenReturn(BigDecimal.valueOf(20.00));

        OrderItemRequest item = new OrderItemRequest(1L, 2);
        CreateOrderRequest request = new CreateOrderRequest("John Doe", "john@test.com", List.of(item));

        // Act & Assert
        assertDoesNotThrow(() -> {
            Order result = orderService.createOrder(request);
            assertNotNull(result);
            assertEquals("John Doe", result.getCustomerName());
            assertEquals(BigDecimal.valueOf(20.00), result.getTotalAmount());
        });
    }
}