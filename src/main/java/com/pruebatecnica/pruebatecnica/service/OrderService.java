package com.pruebatecnica.pruebatecnica.service;

import com.pruebatecnica.pruebatecnica.dto.CreateOrderRequest;
import com.pruebatecnica.pruebatecnica.dto.OrderItemRequest;
//import com.pruebatecnica.pruebatecnica.exception.InsufficientStockException;
//import com.pruebatecnica.pruebatecnica.exception.ProductNotFoundException;
import com.pruebatecnica.pruebatecnica.model.Order;
import com.pruebatecnica.pruebatecnica.model.OrderItem;
import com.pruebatecnica.pruebatecnica.model.OrderStatus;
import com.pruebatecnica.pruebatecnica.model.Product;
import com.pruebatecnica.pruebatecnica.repository.OrderRepository;
//import com.pruebatecnica.pruebatecnica.repository.ProductRepository;
import com.pruebatecnica.pruebatecnica.service.business.OrderCalculator;
import com.pruebatecnica.pruebatecnica.service.validator.OrderValidator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    /*@Autowired
    private ProductRepository productRepository;*/

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderValidator orderValidator;

    @Autowired
    private OrderCalculator orderCalculator;
    
    /**
     * NOTA IMPORTANTE: Este método viola varios principios SOLID intencionalmente.
     * Los candidatos deben refactorizar este código para hacerlo más mantenible y testeable.
     */
    /*@Transactional
    public Order createOrder(CreateOrderRequest request) {
        // TODO: Los candidatos deben refactorizar todo este método
        
        // Validaciones mezcladas con lógica de negocio
        if (request.getCustomerName() == null || request.getCustomerName().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer name is required");
        }
        if (request.getCustomerEmail() == null || request.getCustomerEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer email is required");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order items are required");
        }
        
        // Crear orden
        Order order = new Order(request.getCustomerName(), request.getCustomerEmail());
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        Set<Long> uniqueProductIds = new HashSet<>();
        
        // Procesamiento de items mezclado con validación de stock
        for (OrderItemRequest itemRequest : request.getItems()) {
            if (itemRequest.getProductId() == null) {
                throw new IllegalArgumentException("Product ID is required");
            }
            if (itemRequest.getQuantity() == null || itemRequest.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be greater than 0");
            }
            
            // Buscar producto
            Product product = productRepository.findById(itemRequest.getProductId())
                .orElseThrow(() -> new ProductNotFoundException(itemRequest.getProductId()));
            
            // Validar stock (problema de concurrencia no resuelto)
            if (product.getStock() < itemRequest.getQuantity()) {
                throw new InsufficientStockException(product.getName(), itemRequest.getQuantity(), product.getStock());
            }
            
            // Actualizar stock
            product.setStock(product.getStock() - itemRequest.getQuantity());
            productRepository.save(product);
            
            // Crear item de orden
            OrderItem orderItem = new OrderItem(product, itemRequest.getQuantity());
            orderItem.setOrder(order);
            orderItems.add(orderItem);
            
            // Calcular subtotal
            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            total = total.add(itemTotal);
            
            // Trackear productos únicos para descuento
            uniqueProductIds.add(product.getId());
        }
        
        // Lógica del descuento "Variedad" mezclada con todo lo demás
        // TODO: Los candidatos deben implementar y testear esta funcionalidad
        // Regla: Si el pedido contiene más de 3 tipos de productos diferentes, 
        // aplicar 10% de descuento al total
        if (uniqueProductIds.size() > 3) {
            BigDecimal discount = total.multiply(BigDecimal.valueOf(0.10));
            total = total.subtract(discount);
        }
        
        order.setItems(orderItems);
        order.setTotalAmount(total);
        order.setStatus(OrderStatus.CONFIRMED);
        
        return orderRepository.save(order);
    }*/
    
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
    }
    
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    /**
     * Este es el método ya refactorizado cumpliendo con las tareas 1 y 2 de la parte 1 correspondientes a
     * la refactorización y a la implementación de la Regla del Descuento "Variedad" respectivamente
     */
    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        // Aquí valido el request de la orden
        orderValidator.validateRequest(request);

        Order order = new Order(request.getCustomerName(), request.getCustomerEmail());
        List<OrderItem> orderItems = new ArrayList<>();
        Set<Long> uniqueProductIds = new HashSet<>();

        // Aquí proceso los items, donde se validan los datos, y se verifica y actualiza stock
        for (OrderItemRequest itemRequest : request.getItems()) {
            orderValidator.validateItem(itemRequest);

            Product product = productService.validateAndUpdateStock(itemRequest.getProductId(), itemRequest.getQuantity());

            OrderItem orderItem = new OrderItem(product, itemRequest.getQuantity());
            orderItem.setOrder(order);
            orderItems.add(orderItem);

            uniqueProductIds.add(product.getId());
        }

        // Aquí calculo el total y aplico la regla de descuento "variedad" si cumple las condiciones
        BigDecimal total = orderCalculator.calculateTotal(orderItems);
        total = orderCalculator.applyVarietyDiscount(total, uniqueProductIds);

        //Finalmente .. armo la orden ya completa para la persistencia
        order.setItems(orderItems);
        order.setTotalAmount(total);
        order.setStatus(OrderStatus.CONFIRMED);

        return orderRepository.save(order);
    }
}