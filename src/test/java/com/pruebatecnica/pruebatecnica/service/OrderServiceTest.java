package com.pruebatecnica.pruebatecnica.service;

import com.pruebatecnica.pruebatecnica.dto.CreateOrderRequest;
import com.pruebatecnica.pruebatecnica.dto.OrderItemRequest;
import com.pruebatecnica.pruebatecnica.exception.InsufficientStockException;
import com.pruebatecnica.pruebatecnica.exception.OrderNotFoundException;
import com.pruebatecnica.pruebatecnica.exception.ProductNotFoundException;
import com.pruebatecnica.pruebatecnica.model.Order;
import com.pruebatecnica.pruebatecnica.model.Product;
import com.pruebatecnica.pruebatecnica.repository.OrderRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


/**
 * Suite de Pruebas Unitarias para OrderService.

 * ESTRATEGIA DE TESTING:

 * 1. ESCENARIOS DE DESCUENTO:
 * - testCreateOrderWithoutDiscount_ShouldNotApplyVarietyDiscount:
 * Valida que con 3 productos (No aplica descuento).

 * - testCreateOrderWithDiscount_ShouldApplyVarietyDiscount:
 * Valida que > 3 productos (aplica el 10% de descuento).

 * - testCreateOrderWithSameProductMultipleTimes_ShouldNotApplyDiscount:
 * Asegura que el descuento se base en tipos únicos, no en cantidad total de articulos.

 * - testCreateBasicOrder:
 * Test de flujo base para creación de una orden simple.
 *---------------------------------------------------------------------

 * 2. ESCENARIOS DE ERROR Y EXCEPCIONES:
 * - testCreateOrder_ProductNotFound_ShouldThrowException:
 * Verifica la reacción del sistema ante IDs inexistentes.

 * - testProductNotFoundMessage:
 * Valida que el mensaje de error incluya la información correcta para el usuario.

 * - testCreateOrder_InsufficientStock_ShouldThrowException:
 * Verifica que no se procesen órdenes que superen el stock disponible.

 * - testInsufficientStockMessage:
 * Valida que el error de stock sea informativo para el cliente.

 * - testGetOrderById_NotFound:
 * Verifica el manejo de errores al consultar una orden inexistente.
 *------------------------------------------------------------------------

 * 3. CONSULTAS Y PERSISTENCIA:
 * - testGetAllOrders:
 * Valida la recuperación correcta de la lista histórica de órdenes.

 * - testGetOrderById_Success:
 * Verifica que se pueda recuperar una orden específica correctamente por su ID.
 *-----------------------------------------------------------------------

 * 4. VERIFICACIÓN DE INTEGRIDAD:
 * - Se utiliza verify() con never() en casos de error para asegurar que
 * no existan persistencias accidentales en el repositorio (Atomicidad lógica).
 */

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;


    /// 1. ESCENARIOS DE DESCUENTO
    @Test
    @DisplayName("Debe crear orden sin descuento cuando hay 3 productos diferentes")
    void testCreateOrderWithoutDiscount_ShouldNotApplyVarietyDiscount() {
        // Arrange: 3 productos diferentes (Límite para NO descuento)
        Product p1 = createProduct(1L, "Laptop", 100.0, 10);
        Product p2 = createProduct(2L, "Mouse", 20.0, 10);
        Product p3 = createProduct(3L, "Teclado", 30.0, 10);

        when(productRepository.findByIdWithLock(1L)).thenReturn(Optional.of(p1));
        when(productRepository.findByIdWithLock(2L)).thenReturn(Optional.of(p2));
        when(productRepository.findByIdWithLock(3L)).thenReturn(Optional.of(p3));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        CreateOrderRequest request = new CreateOrderRequest("User", "test@test.com", List.of(
                new OrderItemRequest(1L, 1),
                new OrderItemRequest(2L, 1),
                new OrderItemRequest(3L, 1)
        ));

        // Act
        Order result = orderService.createOrder(request);

        // Assert: 100 + 20 + 30 = 150 (Sin descuento)
        assertEquals(0, new BigDecimal("150.00").compareTo(result.getTotalAmount()));
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @DisplayName("Debe aplicar 10% de descuento cuando hay más de 3 productos diferentes")
    void testCreateOrderWithDiscount_ShouldApplyVarietyDiscount() {
        // Arrange: 4 productos diferentes (Aplica 10% descuento)
        Product p1 = createProduct(1L, "P1", 100.0, 10);
        Product p2 = createProduct(2L, "P2", 100.0, 10);
        Product p3 = createProduct(3L, "P3", 100.0, 10);
        Product p4 = createProduct(4L, "P4", 100.0, 10);

        when(productRepository.findByIdWithLock(anyLong()))
                .thenReturn(Optional.of(p1))
                .thenReturn(Optional.of(p2))
                .thenReturn(Optional.of(p3))
                .thenReturn(Optional.of(p4));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        CreateOrderRequest request = new CreateOrderRequest("User", "test@test.com", List.of(
                new OrderItemRequest(1L, 1),
                new OrderItemRequest(2L, 1),
                new OrderItemRequest(3L, 1),
                new OrderItemRequest(4L, 1)
        ));

        // Act
        Order result = orderService.createOrder(request);

        // Assert: Total 400 - 10% (40) = 360
        assertEquals(0, new BigDecimal("360.00").compareTo(result.getTotalAmount()));
    }

    @Test
    @DisplayName("No debe aplicar descuento si es el mismo producto repetido")
    void testCreateOrderWithSameProductMultipleTimes_ShouldNotApplyDiscount() {
        // Arrange: 1 solo tipo de producto pero mucha cantidad
        Product p1 = createProduct(1L, "Manzana", 10.0, 100);

        when(productRepository.findByIdWithLock(1L)).thenReturn(Optional.of(p1));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        CreateOrderRequest request = new CreateOrderRequest("User", "test@test.com", List.of(
                new OrderItemRequest(1L, 10) // 10 manzanas
        ));

        // Act
        Order result = orderService.createOrder(request);

        // Assert: 10 * 10 = 100 (Sin descuento porque es el mismo ID)
        assertEquals(0, new BigDecimal("100.00").compareTo(result.getTotalAmount()));
    }

    @Test
    @DisplayName("Test Base: Debe crear una orden básica exitosamente")
    void testCreateBasicOrder() {
        // Arrange
        Product product1 = new Product("Test Product", BigDecimal.valueOf(10.00), 5);
        product1.setId(1L);

        when(productRepository.findByIdWithLock(1L)).thenReturn(Optional.of(product1));

        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        OrderItemRequest item = new OrderItemRequest(1L, 2);
        CreateOrderRequest request = new CreateOrderRequest("John Doe", "john@test.com", List.of(item));

        // Act
        Order result = orderService.createOrder(request);

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getCustomerName());
        assertEquals(0, new BigDecimal("20.00").compareTo(result.getTotalAmount()));
        // Verificación de integridad: el stock debe bajar de 5 a 3
        assertEquals(3, product1.getStock());
    }
    //// END - ESCENARIOS DE DESCUENTO

    /// Test Adicionales.
    /// 2. ESCENARIOS DE ERROR Y EXCEPCIONES:
    @Test
    @DisplayName("Debe lanzar excepción si uno de los productos de la lista no existe")
    void testCreateOrder_ProductNotFound_WithMixedItems_ShouldThrowException() {
        // Arrange: Creamos dos productos reales que existen y uno que se va a omitir (p3)
        Product p1 = new Product("Laptop", BigDecimal.valueOf(1000.00), 10);
        p1.setId(1L);
        Product p2 = new Product("Mouse", BigDecimal.valueOf(50.00), 20);
        p2.setId(2L);
        Product p3 = new Product("Iphone", BigDecimal.valueOf(500.00), 5);
        p3.setId(3L);

        when(productRepository.findByIdWithLock(1L)).thenReturn(Optional.of(p1));
        when(productRepository.findByIdWithLock(2L)).thenReturn(Optional.of(p2));
        //when(productRepository.findByIdWithLock(999L)).thenReturn(Optional.empty());//No asignamos p3 para poder ejecutar la prueba

        CreateOrderRequest request = new CreateOrderRequest("John Doe", "john@test.com", List.of(
                new OrderItemRequest(1L, 1),
                new OrderItemRequest(2L, 1),
                new OrderItemRequest(999L, 1) // Item no encontrado
        ));

        // Act & Assert: Verificamos que se lance la excepción Personalizada
        assertThrows(ProductNotFoundException.class, () -> {
            orderService.createOrder(request);
        });
    }

    @Test
    @DisplayName("El mensaje de error debe contener el ID del producto no encontrado")
    void testProductNotFoundMessage() {
        // Arrange
        when(productRepository.findByIdWithLock(99L)).thenReturn(Optional.empty());

        CreateOrderRequest request = new CreateOrderRequest("User", "u@u.com",
                List.of(new OrderItemRequest(99L, 1)));

        // Act
        ProductNotFoundException ex = assertThrows(ProductNotFoundException.class, () -> {
            orderService.createOrder(request);
        });

        // Assert: Buscamos el Id del producto en el mensaje personalizado
        assertTrue(ex.getMessage().contains("99"));
    }

    @Test
    @DisplayName("Debe lanzar excepción y no guardar la orden si no hay stock")
    void testCreateOrder_InsufficientStock_ShouldThrowException() {
        // Arrange: Preparamos un producto con stock insuficiente (solo 2)
        Product p1 = createProduct(1L, "Laptop", 1000.0, 2);
        when(productRepository.findByIdWithLock(1L)).thenReturn(Optional.of(p1));

        // (Conflicto: 5 unidades pedidas > 2 en existencia)
        CreateOrderRequest request = new CreateOrderRequest("Tito", "tito@test.com",
                List.of(new OrderItemRequest(1L, 5)));

        // Act & Assert: Verificamos que lance la excepción personalizada
        assertThrows(InsufficientStockException.class, () -> {
            orderService.createOrder(request);
        });

        // Verificación de seguridad: La orden nunca debe persistirse si hay error de stock
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("El mensaje de error de stock debe ser descriptivo")
    void testInsufficientStockMessage() {
        // Arrange
        Product p1 = createProduct(1L, "Laptop", 1000.0, 2);
        when(productRepository.findByIdWithLock(1L)).thenReturn(Optional.of(p1));

        CreateOrderRequest request = new CreateOrderRequest("Tito", "tito@test.com",
                List.of(new OrderItemRequest(1L, 5)));

        // Act: Verificamos la excepcion personalizada
        InsufficientStockException ex = assertThrows(InsufficientStockException.class, () -> {
            orderService.createOrder(request);
        });

        // Assert
        assertTrue(ex.getMessage().contains("Laptop"), "El mensaje debe contener el nombre del producto");
    }

    @Test
    @DisplayName("Debe lanzar excepción si la orden no existe")
    void testGetOrderById_NotFound() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(OrderNotFoundException.class, () -> orderService.getOrderById(1L));
    }
    ///END

    ///  3. CONSULTAS Y PERSISTENCIA:
    @Test
    @DisplayName("Debe retornar una lista de todas las órdenes")
    void testGetAllOrders() {
        // Arrange
        Order order = new Order();
        when(orderRepository.findAll()).thenReturn(List.of(order));

        // Act
        List<Order> result = orderService.getAllOrders();

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(orderRepository).findAll();
    }

    @Test
    @DisplayName("Debe encontrar y retornar una orden por su ID")
    void testGetOrderById_Success() {
        // Arrange
        Order order = new Order();
        order.setId(1L);
        order.setCustomerName("Tito");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // Act
        Order result = orderService.getOrderById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Tito", result.getCustomerName());
        verify(orderRepository).findById(1L);
    }
    ///END CONSULTAS Y PERSISTENCIA:


    /// Metodo helper para crear productos rápidamente
    private Product createProduct(Long id, String name, Double price, Integer stock) {
        Product p = new Product(name, BigDecimal.valueOf(price), stock);
        p.setId(id);
        return p;
    }
}