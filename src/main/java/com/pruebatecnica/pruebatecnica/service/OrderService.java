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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        Order order = new Order(request.getCustomerName(), request.getCustomerEmail());

        processBusinessLogic(order, request.getItems());

        applyVarietyDiscount(order);

        order.setStatus(OrderStatus.CONFIRMED);

        return orderRepository.save(order);
    }

    private void processBusinessLogic(Order order, List<OrderItemRequest> itemRequests) {
        BigDecimal subtotal = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : itemRequests) {
            Product product = productRepository.findByIdWithLock(itemRequest.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException(itemRequest.getProductId()));

            validateAndUpdateStock(product, itemRequest.getQuantity());

            OrderItem orderItem = new OrderItem(product, itemRequest.getQuantity());
            order.addItem(orderItem);

            subtotal = subtotal.add(orderItem.getTotalPrice());
        }
        order.setTotalAmount(subtotal);
    }

    private void validateAndUpdateStock(Product product, int quantity) {
        if (product.getStock() < quantity) {
            throw new InsufficientStockException(product.getName(), quantity, product.getStock());
        }
        product.setStock(product.getStock() - quantity);
        productRepository.save(product);
    }

    private void applyVarietyDiscount(Order order) {
        long uniqueProductTypes = order.getItems().stream()
                .map(item -> item.getProduct().getId())
                .distinct()
                .count();

        if (uniqueProductTypes > 3) {
            BigDecimal totalOriginal = order.getTotalAmount();
            BigDecimal discount = totalOriginal.multiply(new BigDecimal("0.10"));
            order.setTotalAmount(totalOriginal.subtract(discount));
        }
    }

    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
}