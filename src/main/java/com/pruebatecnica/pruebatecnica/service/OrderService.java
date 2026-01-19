package com.pruebatecnica.pruebatecnica.service;

import com.pruebatecnica.pruebatecnica.dto.CreateOrderRequest;
import com.pruebatecnica.pruebatecnica.dto.OrderItemRequest;
import com.pruebatecnica.pruebatecnica.exception.ProductNotFoundException;
import com.pruebatecnica.pruebatecnica.model.Order;
import com.pruebatecnica.pruebatecnica.model.OrderItem;
import com.pruebatecnica.pruebatecnica.model.OrderStatus;
import com.pruebatecnica.pruebatecnica.model.Product;
import com.pruebatecnica.pruebatecnica.repository.OrderRepository;
import com.pruebatecnica.pruebatecnica.repository.ProductRepository;
import com.pruebatecnica.pruebatecnica.validation.OrderRequestValidator;
import com.pruebatecnica.pruebatecnica.validation.StockValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private OrderRequestValidator orderRequestValidator;
    
    @Autowired
    private StockValidator stockValidator;
    
    @Autowired
    private PricingService pricingService;
    
    @Autowired
    private DiscountService discountService;
    
    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        orderRequestValidator.validate(request);
        
        Order order = createOrderEntity(request);
        List<OrderItem> orderItems = createOrderItems(request.getItems(), order);
        List<Long> productIds = extractProductIds(request.getItems());
        
        BigDecimal subtotal = pricingService.calculateOrderTotal(orderItems);
        BigDecimal discount = discountService.calculateDiscount(productIds, subtotal);
        BigDecimal finalTotal = pricingService.applyDiscount(subtotal, discount);
        
        finalizeOrder(order, orderItems, finalTotal);
        
        return orderRepository.save(order);
    }
    
    private Order createOrderEntity(CreateOrderRequest request) {
        return new Order(request.getCustomerName(), request.getCustomerEmail());
    }
    
    private List<OrderItem> createOrderItems(List<OrderItemRequest> itemRequests, Order order) {
        List<OrderItem> orderItems = new ArrayList<>();
        
        for (OrderItemRequest itemRequest : itemRequests) {
            Product product = findProductById(itemRequest.getProductId());
            stockValidator.validateStockAvailability(product, itemRequest.getQuantity());
            stockValidator.reserveStock(product, itemRequest.getQuantity());
            productRepository.save(product);
            
            OrderItem orderItem = createOrderItem(product, itemRequest.getQuantity(), order);
            orderItems.add(orderItem);
        }
        
        return orderItems;
    }
    
    private Product findProductById(Long productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));
    }
    
    private OrderItem createOrderItem(Product product, Integer quantity, Order order) {
        OrderItem orderItem = new OrderItem(product, quantity);
        orderItem.setOrder(order);
        return orderItem;
    }
    
    private List<Long> extractProductIds(List<OrderItemRequest> itemRequests) {
        return itemRequests.stream()
            .map(OrderItemRequest::getProductId)
            .toList();
    }
    
    private void finalizeOrder(Order order, List<OrderItem> orderItems, BigDecimal totalAmount) {
        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.CONFIRMED);
    }
    
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
    }
    
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
}