package com.pruebatecnica.pruebatecnica.service;

import com.pruebatecnica.pruebatecnica.dto.CreateOrderRequest;
import com.pruebatecnica.pruebatecnica.dto.OrderItemRequest;
import com.pruebatecnica.pruebatecnica.model.Order;
import com.pruebatecnica.pruebatecnica.model.Product;
import com.pruebatecnica.pruebatecnica.repository.OrderRepository;
import com.pruebatecnica.pruebatecnica.repository.ProductRepository;
import com.pruebatecnica.pruebatecnica.validation.OrderRequestValidator;
import com.pruebatecnica.pruebatecnica.validation.StockValidator;
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

    @Mock
    private OrderRequestValidator orderRequestValidator;

    @Mock
    private StockValidator stockValidator;

    @Mock
    private PricingService pricingService;

    @Mock
    private DiscountService discountService;

    @InjectMocks
    private OrderService orderService;

    @Test
    void testCreateOrderWithoutDiscount_ShouldNotApplyVarietyDiscount() {
        // Arrange
        Product product1 = new Product("Product 1", BigDecimal.valueOf(10.00), 5);
        Product product2 = new Product("Product 2", BigDecimal.valueOf(15.00), 5);
        Product product3 = new Product("Product 3", BigDecimal.valueOf(20.00), 5);
        
        product1.setId(1L);
        product2.setId(2L);
        product3.setId(3L);

        OrderItemRequest item1 = new OrderItemRequest(1L, 1);
        OrderItemRequest item2 = new OrderItemRequest(2L, 1);
        OrderItemRequest item3 = new OrderItemRequest(3L, 1);
        
        CreateOrderRequest request = new CreateOrderRequest(
            "John Doe", 
            "john@test.com", 
            List.of(item1, item2, item3)
        );

        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product2));
        when(productRepository.findById(3L)).thenReturn(Optional.of(product3));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);
        
        when(pricingService.calculateOrderTotal(any())).thenReturn(BigDecimal.valueOf(45.00));
        when(discountService.calculateDiscount(any(), any())).thenReturn(BigDecimal.ZERO);
        when(pricingService.applyDiscount(any(), any())).thenReturn(BigDecimal.valueOf(45.00));

        // Act
        Order result = orderService.createOrder(request);

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getCustomerName());
        assertEquals(BigDecimal.valueOf(45.00), result.getTotalAmount());
        verify(discountService).calculateDiscount(any(), eq(BigDecimal.valueOf(45.00)));
        verify(pricingService).applyDiscount(BigDecimal.valueOf(45.00), BigDecimal.ZERO);
    }

    @Test
    void testCreateOrderWithDiscount_ShouldApplyVarietyDiscount() {
        // Arrange
        Product product1 = new Product("Product 1", BigDecimal.valueOf(10.00), 5);
        Product product2 = new Product("Product 2", BigDecimal.valueOf(15.00), 5);
        Product product3 = new Product("Product 3", BigDecimal.valueOf(20.00), 5);
        Product product4 = new Product("Product 4", BigDecimal.valueOf(25.00), 5);
        
        product1.setId(1L);
        product2.setId(2L);
        product3.setId(3L);
        product4.setId(4L);

        OrderItemRequest item1 = new OrderItemRequest(1L, 1);
        OrderItemRequest item2 = new OrderItemRequest(2L, 1);
        OrderItemRequest item3 = new OrderItemRequest(3L, 1);
        OrderItemRequest item4 = new OrderItemRequest(4L, 1);
        
        CreateOrderRequest request = new CreateOrderRequest(
            "John Doe", 
            "john@test.com", 
            List.of(item1, item2, item3, item4)
        );

        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product2));
        when(productRepository.findById(3L)).thenReturn(Optional.of(product3));
        when(productRepository.findById(4L)).thenReturn(Optional.of(product4));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);
        
        when(pricingService.calculateOrderTotal(any())).thenReturn(BigDecimal.valueOf(70.00));
        when(discountService.calculateDiscount(any(), any())).thenReturn(BigDecimal.valueOf(7.00));
        when(pricingService.applyDiscount(any(), any())).thenReturn(BigDecimal.valueOf(63.00));

        // Act
        Order result = orderService.createOrder(request);

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getCustomerName());
        assertEquals(BigDecimal.valueOf(63.00), result.getTotalAmount());
        verify(discountService).calculateDiscount(any(), eq(BigDecimal.valueOf(70.00)));
        verify(pricingService).applyDiscount(BigDecimal.valueOf(70.00), BigDecimal.valueOf(7.00));
    }

    @Test
    void testCreateOrderWithSameProductMultipleTimes_ShouldNotApplyDiscount() {
        // Arrange
        Product product = new Product("Apple", BigDecimal.valueOf(5.00), 50);
        product.setId(1L);

        OrderItemRequest item = new OrderItemRequest(1L, 10);
        CreateOrderRequest request = new CreateOrderRequest(
            "John Doe", 
            "john@test.com", 
            List.of(item)
        );

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);
        
        when(pricingService.calculateOrderTotal(any())).thenReturn(BigDecimal.valueOf(50.00));
        when(discountService.calculateDiscount(any(), any())).thenReturn(BigDecimal.ZERO);
        when(pricingService.applyDiscount(any(), any())).thenReturn(BigDecimal.valueOf(50.00));

        // Act
        Order result = orderService.createOrder(request);

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getCustomerName());
        assertEquals(BigDecimal.valueOf(50.00), result.getTotalAmount());
        verify(discountService).calculateDiscount(any(), eq(BigDecimal.valueOf(50.00)));
        verify(pricingService).applyDiscount(BigDecimal.valueOf(50.00), BigDecimal.ZERO);
    }

    @Test
    void testCreateBasicOrder() {
        // Arrange
        Product product1 = new Product("Test Product", BigDecimal.valueOf(10.00), 5);
        product1.setId(1L);
        
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        OrderItemRequest item = new OrderItemRequest(1L, 2);
        CreateOrderRequest request = new CreateOrderRequest("John Doe", "john@test.com", List.of(item));
        
        when(pricingService.calculateOrderTotal(any())).thenReturn(BigDecimal.valueOf(20.00));
        when(discountService.calculateDiscount(any(), any())).thenReturn(BigDecimal.ZERO);
        when(pricingService.applyDiscount(any(), any())).thenReturn(BigDecimal.valueOf(20.00));

        // Act
        Order result = orderService.createOrder(request);

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getCustomerName());
        assertEquals(BigDecimal.valueOf(20.00), result.getTotalAmount());
        
        // Verify interactions
        verify(orderRequestValidator).validate(request);
        verify(stockValidator).validateStockAvailability(product1, 2);
        verify(stockValidator).reserveStock(product1, 2);
        verify(productRepository).save(product1);
        verify(orderRepository).save(any(Order.class));
    }
}