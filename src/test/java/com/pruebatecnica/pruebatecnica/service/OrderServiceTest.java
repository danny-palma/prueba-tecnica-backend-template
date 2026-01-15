package com.pruebatecnica.pruebatecnica.service;

import com.pruebatecnica.pruebatecnica.dto.CreateOrderRequest;
import com.pruebatecnica.pruebatecnica.dto.OrderItemRequest;
import com.pruebatecnica.pruebatecnica.model.Order;
import com.pruebatecnica.pruebatecnica.model.OrderItem;
import com.pruebatecnica.pruebatecnica.model.Product;
import com.pruebatecnica.pruebatecnica.repository.OrderRepository;
import com.pruebatecnica.pruebatecnica.repository.ProductRepository;
import com.pruebatecnica.pruebatecnica.service.pricing.DiscountService;
import com.pruebatecnica.pruebatecnica.service.pricing.PriceCalculator;
import com.pruebatecnica.pruebatecnica.service.validation.OrderInputValidator;
import com.pruebatecnica.pruebatecnica.service.validation.StockValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests de integración para OrderService.
 * Verifica el flujo completo de creación de pedidos incluyendo descuentos.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    // Usamos @Spy para usar la implementación real de los validadores y calculadores
    @Spy
    private OrderInputValidator inputValidator;

    @Spy
    private StockValidator stockValidator;

    @Spy
    private PriceCalculator priceCalculator;

    @Spy
    private DiscountService discountService;

    @InjectMocks
    private OrderService orderService;

    // ==================== TESTS: SIN DESCUENTO ====================

    @Test
    @DisplayName("NO descuento: pedido con 3 tipos de productos")
    void testCreateOrderWithoutDiscount_ShouldNotApplyVarietyDiscount() {
        // Arrange: 3 productos diferentes (NO aplica descuento)
        Product manzana = createProduct(1L, "Manzana", BigDecimal.valueOf(10.00), 100);
        Product pera = createProduct(2L, "Pera", BigDecimal.valueOf(10.00), 100);
        Product uva = createProduct(3L, "Uva", BigDecimal.valueOf(10.00), 100);

        when(productRepository.findById(1L)).thenReturn(Optional.of(manzana));
        when(productRepository.findById(2L)).thenReturn(Optional.of(pera));
        when(productRepository.findById(3L)).thenReturn(Optional.of(uva));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        List<OrderItemRequest> items = List.of(
            new OrderItemRequest(1L, 1),
            new OrderItemRequest(2L, 1),
            new OrderItemRequest(3L, 1)
        );
        CreateOrderRequest request = new CreateOrderRequest("Juan", "juan@test.com", items);

        // Act
        Order result = orderService.createOrder(request);

        // Assert: Subtotal = 30, sin descuento = 30
        assertNotNull(result);
        assertEquals(0, BigDecimal.valueOf(30.00).compareTo(result.getTotalAmount()));
    }

    // ==================== TESTS: CON DESCUENTO ====================

    @Test
    @DisplayName("SÍ descuento: pedido con 4 tipos de productos = 10%")
    void testCreateOrderWithDiscount_ShouldApplyVarietyDiscount() {
        // Arrange: 4 productos diferentes (SÍ aplica 10% descuento)
        Product p1 = createProduct(1L, "Producto 1", BigDecimal.valueOf(25.00), 100);
        Product p2 = createProduct(2L, "Producto 2", BigDecimal.valueOf(25.00), 100);
        Product p3 = createProduct(3L, "Producto 3", BigDecimal.valueOf(25.00), 100);
        Product p4 = createProduct(4L, "Producto 4", BigDecimal.valueOf(25.00), 100);

        when(productRepository.findById(1L)).thenReturn(Optional.of(p1));
        when(productRepository.findById(2L)).thenReturn(Optional.of(p2));
        when(productRepository.findById(3L)).thenReturn(Optional.of(p3));
        when(productRepository.findById(4L)).thenReturn(Optional.of(p4));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        List<OrderItemRequest> items = List.of(
            new OrderItemRequest(1L, 1),
            new OrderItemRequest(2L, 1),
            new OrderItemRequest(3L, 1),
            new OrderItemRequest(4L, 1)
        );
        CreateOrderRequest request = new CreateOrderRequest("María", "maria@test.com", items);

        // Act
        Order result = orderService.createOrder(request);

        // Assert: Subtotal = 100, con 10% descuento = 90
        assertNotNull(result);
        assertEquals(0, BigDecimal.valueOf(90.00).compareTo(result.getTotalAmount()));
    }

    // ==================== TESTS: MÚLTIPLES UNIDADES ====================

    @Test
    @DisplayName("NO descuento: 10 unidades del mismo producto = 1 tipo")
    void testCreateOrderWithSameProductMultipleTimes_ShouldNotApplyDiscount() {
        // Arrange: 10 unidades de Manzana = solo 1 tipo
        Product manzana = createProduct(1L, "Manzana", BigDecimal.valueOf(5.00), 100);

        when(productRepository.findById(1L)).thenReturn(Optional.of(manzana));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        List<OrderItemRequest> items = List.of(
            new OrderItemRequest(1L, 10)
        );
        CreateOrderRequest request = new CreateOrderRequest("Pedro", "pedro@test.com", items);

        // Act
        Order result = orderService.createOrder(request);

        // Assert: Subtotal = 50, sin descuento = 50
        assertNotNull(result);
        assertEquals(0, BigDecimal.valueOf(50.00).compareTo(result.getTotalAmount()));
    }

    // ==================== TEST BÁSICO ====================

    @Test
    @DisplayName("Crear pedido básico correctamente")
    void testCreateBasicOrder() {
        // Arrange
        Product product = createProduct(1L, "Test Product", BigDecimal.valueOf(10.00), 50);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        OrderItemRequest item = new OrderItemRequest(1L, 2);
        CreateOrderRequest request = new CreateOrderRequest("John Doe", "john@test.com", List.of(item));

        // Act
        Order result = orderService.createOrder(request);

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getCustomerName());
        assertEquals(0, BigDecimal.valueOf(20.00).compareTo(result.getTotalAmount()));
    }

    // ==================== MÉTODO AUXILIAR ====================

    /**
     * Crea un producto de prueba.
     */
    private Product createProduct(Long id, String name, BigDecimal price, int stock) {
        Product product = new Product(name, price, stock);
        product.setId(id);
        return product;
    }
}
