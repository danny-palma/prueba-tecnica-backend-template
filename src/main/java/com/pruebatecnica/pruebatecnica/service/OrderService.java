package com.pruebatecnica.pruebatecnica.service;

import com.pruebatecnica.pruebatecnica.dto.CreateOrderRequest;
import com.pruebatecnica.pruebatecnica.model.Order;
import com.pruebatecnica.pruebatecnica.repository.OrderRepository;
import com.pruebatecnica.pruebatecnica.service.calculator.OrderCalculatorService;
import com.pruebatecnica.pruebatecnica.service.factory.OrderFactory;
import com.pruebatecnica.pruebatecnica.service.validator.OrderRequestValidator;
import com.pruebatecnica.pruebatecnica.service.validator.StockValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderRequestValidator orderRequestValidator;
    private final StockValidator stockValidator;
    private final OrderFactory orderFactory;
    private final OrderCalculatorService orderCalculator;

    public OrderService(OrderRepository orderRepository,
                        OrderRequestValidator orderRequestValidator,
                        StockValidator stockValidator,
                        OrderFactory orderFactory,
                        OrderCalculatorService orderCalculator) {

        this.orderRepository = orderRepository;
        this.orderRequestValidator = orderRequestValidator;
        this.stockValidator = stockValidator;
        this.orderFactory = orderFactory;
        this.orderCalculator = orderCalculator;
    }

    @Transactional
    public Order createOrder(CreateOrderRequest request) {

        // 1️ Validación de request
        orderRequestValidator.validate(request);

        // 2️ Validación de stock
        stockValidator.validateStock(request);

        // 3️ Construcción de orden + items
        Order order = orderFactory.buildOrder(request);

        // 4️ Cálculo de precios + descuentos
        orderCalculator.calculateTotals(order);

        // 5️ Guardar en DB
        return orderRepository.save(order);
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Order not found: " + id));
    }

    public java.util.List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
}
