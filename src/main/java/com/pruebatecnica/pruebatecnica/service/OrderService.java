package com.pruebatecnica.pruebatecnica.service;

import com.pruebatecnica.pruebatecnica.dto.CreateOrderRequest;
import com.pruebatecnica.pruebatecnica.model.Order;
import java.util.List;

public interface OrderService {
    Order createOrder(CreateOrderRequest request);

    Order getOrderById(Long orderId);

    List<Order> getAllOrders();
}