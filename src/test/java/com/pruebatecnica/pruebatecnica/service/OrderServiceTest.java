package com.pruebatecnica.pruebatecnica.service;

import com.pruebatecnica.pruebatecnica.dto.CreateOrderRequest;
import com.pruebatecnica.pruebatecnica.dto.OrderItemRequest;
import com.pruebatecnica.pruebatecnica.model.Order;
import com.pruebatecnica.pruebatecnica.model.Product;
import com.pruebatecnica.pruebatecnica.repository.OrderRepository;
import com.pruebatecnica.pruebatecnica.repository.ProductRepository;
import com.pruebatecnica.pruebatecnica.service.validator.StockValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
    private StockValidator stockValidator;

    @Mock
    private PriceCalculator priceCalculator;

    @Mock
    private DiscountService discountService;

    @Mock
    private StockManager stockManager;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void testCreateOrderWithoutDiscount_ShouldNotApplyVarietyDiscount() {
        // Arrange: Crear 3 productos diferentes (umbral es > 3, así que NO debe aplicar descuento)
        Product product1 = new Product("Manzana", BigDecimal.valueOf(5.00), 10);
        product1.setId(1L);
        Product product2 = new Product("Pera", BigDecimal.valueOf(6.00), 10);
        product2.setId(2L);
        Product product3 = new Product("Uva", BigDecimal.valueOf(7.00), 10);
        product3.setId(3L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product2));
        when(productRepository.findById(3L)).thenReturn(Optional.of(product3));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        // Mockear cálculos de precio
        when(priceCalculator.calculateItemPrice(product1, 1)).thenReturn(BigDecimal.valueOf(5.00));
        when(priceCalculator.calculateItemPrice(product2, 1)).thenReturn(BigDecimal.valueOf(6.00));
        when(priceCalculator.calculateItemPrice(product3, 1)).thenReturn(BigDecimal.valueOf(7.00));

        BigDecimal totalSinDescuento = BigDecimal.valueOf(18.00); // 5 + 6 + 7
        when(priceCalculator.calculateOrderTotal(anyList())).thenReturn(totalSinDescuento);

        // Mockear descuento: NO debe aplicar porque solo hay 3 productos (umbral es > 3)
        when(discountService.applyVarietyDiscount(totalSinDescuento, Set.of(1L, 2L, 3L)))
                .thenReturn(totalSinDescuento); // Sin descuento

        OrderItemRequest item1 = new OrderItemRequest(1L, 1);
        OrderItemRequest item2 = new OrderItemRequest(2L, 1);
        OrderItemRequest item3 = new OrderItemRequest(3L, 1);
        CreateOrderRequest request = new CreateOrderRequest("John Doe", "john@test.com",
                List.of(item1, item2, item3));

        // Act
        Order result = orderService.createOrder(request);

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getCustomerName());
        assertEquals(BigDecimal.valueOf(18.00), result.getTotalAmount()); // Sin descuento
        assertEquals(3, result.getItems().size());

        // Verificar que se llamó al descuento pero no se aplicó
        verify(discountService).applyVarietyDiscount(totalSinDescuento, Set.of(1L, 2L, 3L));
    }

    @Test
    void testCreateOrderWithDiscount_ShouldApplyVarietyDiscount() {
        // Arrange: Crear 4 productos diferentes (más de 3, así que SÍ debe aplicar descuento del 10%)
        Product product1 = new Product("Manzana", BigDecimal.valueOf(10.00), 10);
        product1.setId(1L);
        Product product2 = new Product("Pera", BigDecimal.valueOf(10.00), 10);
        product2.setId(2L);
        Product product3 = new Product("Uva", BigDecimal.valueOf(10.00), 10);
        product3.setId(3L);
        Product product4 = new Product("Sandía", BigDecimal.valueOf(10.00), 10);
        product4.setId(4L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product2));
        when(productRepository.findById(3L)).thenReturn(Optional.of(product3));
        when(productRepository.findById(4L)).thenReturn(Optional.of(product4));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        // Mockear cálculos de precio
        when(priceCalculator.calculateItemPrice(product1, 1)).thenReturn(BigDecimal.valueOf(10.00));
        when(priceCalculator.calculateItemPrice(product2, 1)).thenReturn(BigDecimal.valueOf(10.00));
        when(priceCalculator.calculateItemPrice(product3, 1)).thenReturn(BigDecimal.valueOf(10.00));
        when(priceCalculator.calculateItemPrice(product4, 1)).thenReturn(BigDecimal.valueOf(10.00));

        BigDecimal totalSinDescuento = BigDecimal.valueOf(40.00); // 10 * 4
        when(priceCalculator.calculateOrderTotal(anyList())).thenReturn(totalSinDescuento);

        // Mockear descuento: SÍ debe aplicar porque hay 4 productos (más de 3)
        // Descuento del 10%: 40.00 * 0.10 = 4.00, entonces 40.00 - 4.00 = 36.00
        BigDecimal totalConDescuento = BigDecimal.valueOf(36.00);
        when(discountService.applyVarietyDiscount(totalSinDescuento, Set.of(1L, 2L, 3L, 4L)))
                .thenReturn(totalConDescuento);

        OrderItemRequest item1 = new OrderItemRequest(1L, 1);
        OrderItemRequest item2 = new OrderItemRequest(2L, 1);
        OrderItemRequest item3 = new OrderItemRequest(3L, 1);
        OrderItemRequest item4 = new OrderItemRequest(4L, 1);
        CreateOrderRequest request = new CreateOrderRequest("John Doe", "john@test.com",
                List.of(item1, item2, item3, item4));

        // Act
        Order result = orderService.createOrder(request);

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getCustomerName());
        assertEquals(BigDecimal.valueOf(36.00), result.getTotalAmount()); // Con descuento del 10%
        assertEquals(4, result.getItems().size());

        // Verificar que se aplicó el descuento
        verify(discountService).applyVarietyDiscount(totalSinDescuento, Set.of(1L, 2L, 3L, 4L));
    }

    @Test
    void testCreateOrderWithSameProductMultipleTimes_ShouldNotApplyDiscount() {
        // Arrange: 10 unidades del mismo producto (solo 1 tipo de producto, NO debe aplicar descuento)
        Product product1 = new Product("Manzana", BigDecimal.valueOf(5.00), 20);
        product1.setId(1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        // Mockear cálculos de precio: 10 unidades * 5.00 = 50.00
        when(priceCalculator.calculateItemPrice(product1, 10)).thenReturn(BigDecimal.valueOf(50.00));

        BigDecimal totalSinDescuento = BigDecimal.valueOf(50.00);
        when(priceCalculator.calculateOrderTotal(anyList())).thenReturn(totalSinDescuento);

        // Mockear descuento: NO debe aplicar porque solo hay 1 tipo de producto
        when(discountService.applyVarietyDiscount(totalSinDescuento, Set.of(1L)))
                .thenReturn(totalSinDescuento); // Sin descuento

        OrderItemRequest item = new OrderItemRequest(1L, 10); // 10 manzanas
        CreateOrderRequest request = new CreateOrderRequest("John Doe", "john@test.com", List.of(item));

        // Act
        Order result = orderService.createOrder(request);

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getCustomerName());
        assertEquals(BigDecimal.valueOf(50.00), result.getTotalAmount()); // Sin descuento
        assertEquals(1, result.getItems().size());
        assertEquals(10, result.getItems().get(0).getQuantity());

        // Verificar que se llamó al descuento pero no se aplicó (solo 1 producto único)
        verify(discountService).applyVarietyDiscount(totalSinDescuento, Set.of(1L));
    }

    @Test
    void testCreateBasicOrder() {
        // Arrange
        Product product1 = new Product("Test Product", BigDecimal.valueOf(10.00), 5);
        product1.setId(1L);
        
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        // Mockear los servicios inyectados
        when(priceCalculator.calculateItemPrice(product1, 2)).thenReturn(BigDecimal.valueOf(20.00));
        when(priceCalculator.calculateOrderTotal(anyList())).thenReturn(BigDecimal.valueOf(20.00));

        // Solo 1 producto, no aplica descuento
        when(discountService.applyVarietyDiscount(BigDecimal.valueOf(20.00), Set.of(1L)))
                .thenReturn(BigDecimal.valueOf(20.00));

        OrderItemRequest item = new OrderItemRequest(1L, 2);
        CreateOrderRequest request = new CreateOrderRequest("John Doe", "john@test.com", List.of(item));

        // Act
        Order result = orderService.createOrder(request);

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getCustomerName());
        assertEquals("john@test.com", result.getCustomerEmail());
        assertEquals(BigDecimal.valueOf(20.00), result.getTotalAmount());
        assertEquals(1, result.getItems().size());

        // Verificar que se llamaron los servicios necesarios
        verify(productRepository).findById(1L);
        verify(stockValidator).validateStockAvailability(product1, 2);
        verify(stockManager).reduceStock(product1, 2);
        verify(priceCalculator).calculateItemPrice(product1, 2);
        verify(priceCalculator).calculateOrderTotal(anyList());
        verify(discountService).applyVarietyDiscount(BigDecimal.valueOf(20.00), Set.of(1L));
        verify(orderRepository).save(any(Order.class));
    }
}