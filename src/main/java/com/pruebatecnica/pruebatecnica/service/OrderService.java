package com.pruebatecnica.pruebatecnica.service;

import com.pruebatecnica.pruebatecnica.dto.CreateOrderRequest;
import com.pruebatecnica.pruebatecnica.dto.OrderItemRequest;
import com.pruebatecnica.pruebatecnica.exception.InsufficientStockException;
import com.pruebatecnica.pruebatecnica.exception.ProductNotFoundException;
import com.pruebatecnica.pruebatecnica.model.Order;
import com.pruebatecnica.pruebatecnica.model.OrderItem;
import com.pruebatecnica.pruebatecnica.model.OrderStatus;
import com.pruebatecnica.pruebatecnica.model.Product;
import com.pruebatecnica.pruebatecnica.repository.OrderRepository;
import com.pruebatecnica.pruebatecnica.repository.ProductRepository;
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

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }
    
    /**
     * NOTA IMPORTANTE: Este método viola varios principios SOLID intencionalmente.
     * Los candidatos deben refactorizar este código para hacerlo más mantenible y testeable.
     */
    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        // TODO: Los candidatos deben refactorizar todo este método
        
        // Validaciones mezcladas con lógica de negocio
        validarRequest(request);
        
        // Crear orden
        Order order = crearOrder(request);        List<OrderItem> orderItems = request.getItems().stream()
                .map(item -> this.getOrderItem(item, order))
                .toList();
        BigDecimal total = calcularSubTotal(orderItems);
        total = calcularDescuento(orderItems, total);
        actualizarOrder(order,orderItems, total);
        return orderRepository.save(order);
    }
    private void validarRequest(CreateOrderRequest request){
        if (isBlank(request.getCustomerName())) {
            throw new IllegalArgumentException("Customer name is required");
        }
        if (isBlank(request.getCustomerEmail())) {
            throw new IllegalArgumentException("Customer email is required");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order items are required");
        }
    }
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private Order crearOrder(CreateOrderRequest request){
        return new Order(request.getCustomerName(), request.getCustomerEmail());
    }

    private OrderItem getOrderItem(OrderItemRequest item, Order order){
        Product product = getProduct(item.getProductId());
        validarStock(product,item);
        actualizarStock(product, item);
        return crearItem(product, item, order);
    }

    private Product getProduct(Long id){
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }
    private void validarStock(Product product, OrderItemRequest item){
        if (product.getStock() < item.getQuantity()) {
            throw new InsufficientStockException(product.getName(), item.getQuantity(), product.getStock());
        }
    }
    private void actualizarStock(Product product, OrderItemRequest item){
        product.setStock(product.getStock() - item.getQuantity());
        productRepository.save(product);
    }
    private OrderItem crearItem(Product product, OrderItemRequest itemRequest, Order order){
        OrderItem item = new OrderItem(product, itemRequest.getQuantity());
        item.setOrder(order);
        return item;
    }
    private BigDecimal calcularSubTotal (List<OrderItem> orderItems){
        return orderItems.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Lógica del descuento "Variedad" mezclada con todo lo demás
    // TODO: Los candidatos deben implementar y testear esta funcionalidad
    // Regla: Si el pedido contiene más de 3 tipos de productos diferentes,
    // aplicar 10% de descuento al total
    private BigDecimal calcularDescuento(List<OrderItem> items, BigDecimal total){
        long uniqueProducts = items.stream()
                .map(item -> item.getProduct().getId())
                .distinct()
                .count();
        if (uniqueProducts > 3) {
            return total.subtract(total.multiply(BigDecimal.valueOf(0.10)));
        }
        return total;
    }
    private void actualizarOrder(Order order, List<OrderItem> items, BigDecimal total) {
        order.setItems(items);
        order.setTotalAmount(total);
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