package com.pruebatecnica.pruebatecnica.service;

import com.pruebatecnica.pruebatecnica.dto.CreateOrderRequest;
import com.pruebatecnica.pruebatecnica.dto.OrderItemRequest;
import com.pruebatecnica.pruebatecnica.model.Order;
import com.pruebatecnica.pruebatecnica.model.OrderItem;
import com.pruebatecnica.pruebatecnica.model.OrderStatus;
import com.pruebatecnica.pruebatecnica.model.Product;
import com.pruebatecnica.pruebatecnica.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Servicio encargado de la gestión de pedidos (órdenes).
 * Coordina el proceso de validación, gestión de inventario, cálculo de precios
 * y persistencia.
 */
@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderValidator orderValidator;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private PriceCalculator priceCalculator;

    /**
     * Crea un nuevo pedido siguiendo el flujo de validación, procesamiento de
     * ítems,
     * cálculo de totales (incluyendo descuentos) y almacenamiento en base de datos.
     *
     * @param request Datos de la solicitud para crear el pedido.
     * @return El pedido creado y persistido.
     */
    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        orderValidator.validate(request);

        Order order = new Order(request.getCustomerName(), request.getCustomerEmail());
        List<OrderItem> items = processOrderItems(request.getItems(), order);

        order.setItems(items);
        order.setTotalAmount(priceCalculator.calculateTotal(items));
        order.setStatus(OrderStatus.CONFIRMED);

        return orderRepository.save(order);
    }

    /**
     * Procesa la lista de solicitudes de ítems para un pedido.
     * Valida el stock de cada producto y deduce la cantidad solicitada.
     *
     * @param itemRequests Lista de ítems solicitados.
     * @param order        El pedido al cual pertenecen los ítems.
     * @return Lista de ítems de pedido procesados y listos para asignar al pedido.
     */
    private List<OrderItem> processOrderItems(List<OrderItemRequest> itemRequests, Order order) {
        List<OrderItem> items = new ArrayList<>();
        for (OrderItemRequest itemRequest : itemRequests) {
            Product product = inventoryService.getAndValidateStock(
                    itemRequest.getProductId(),
                    itemRequest.getQuantity());

            inventoryService.deductStock(product, itemRequest.getQuantity());

            OrderItem orderItem = new OrderItem(product, itemRequest.getQuantity());
            orderItem.setOrder(order);
            items.add(orderItem);
        }
        return items;
    }

    /**
     * Obtiene un pedido por su identificador único.
     *
     * @param orderId ID del pedido.
     * @return El pedido encontrado.
     * @throws RuntimeException Si el pedido no existe.
     */
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
    }

    /**
     * Recupera todos los pedidos registrados en el sistema.
     *
     * @return Lista de todos los pedidos.
     */
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
}