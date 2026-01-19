package com.pruebatecnica.pruebatecnica.service;

import com.pruebatecnica.pruebatecnica.dto.CreateOrderRequest;
import com.pruebatecnica.pruebatecnica.dto.OrderItemRequest;
import com.pruebatecnica.pruebatecnica.model.Order;
import com.pruebatecnica.pruebatecnica.model.Product;
import com.pruebatecnica.pruebatecnica.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderValidator orderValidator;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private PriceCalculator priceCalculator;

    @InjectMocks
    private OrderService orderService;

    @Test
    void testCreateOrder_Success() {
        // Arrange
        OrderItemRequest itemRequest = new OrderItemRequest(1L, 2);
        CreateOrderRequest request = new CreateOrderRequest("John Doe", "john@test.com", List.of(itemRequest));

        Product product = new Product("Test Product", BigDecimal.valueOf(10.00), 10);
        product.setId(1L);

        when(inventoryService.getAndValidateStock(1L, 2)).thenReturn(product);
        when(priceCalculator.calculateTotal(anyList())).thenReturn(BigDecimal.valueOf(20.00));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Order result = orderService.createOrder(request);

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getCustomerName());
        assertEquals("john@test.com", result.getCustomerEmail());
        assertEquals(BigDecimal.valueOf(20.00), result.getTotalAmount());

        verify(orderValidator).validate(request);
        verify(inventoryService).getAndValidateStock(1L, 2);
        verify(inventoryService).deductStock(product, 2);
        verify(priceCalculator).calculateTotal(anyList());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void testGetOrderById() {
        Order order = new Order("John", "john@test.com");
        when(orderRepository.findById(1L)).thenReturn(java.util.Optional.of(order));

        Order result = orderService.getOrderById(1L);

        assertNotNull(result);
        assertEquals("John", result.getCustomerName());
    }

    @Test
    void testGetOrderById_NotFound() {
        when(orderRepository.findById(1L)).thenReturn(java.util.Optional.empty());

        assertThrows(RuntimeException.class, () -> orderService.getOrderById(1L));
    }

    @Test
    void testGetAllOrders() {
        when(orderRepository.findAll()).thenReturn(List.of(new Order("John", "john@test.com")));

        List<Order> result = orderService.getAllOrders();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }
}
