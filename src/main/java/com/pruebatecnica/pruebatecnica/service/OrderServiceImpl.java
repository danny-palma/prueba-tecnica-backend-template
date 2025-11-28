package com.pruebatecnica.pruebatecnica.service;

import com.pruebatecnica.pruebatecnica.dto.CreateOrderRequest;
import com.pruebatecnica.pruebatecnica.dto.OrderItemRequest;
import com.pruebatecnica.pruebatecnica.exception.personalized.BadRequestException;
import com.pruebatecnica.pruebatecnica.exception.personalized.ProductNotFoundException;
import com.pruebatecnica.pruebatecnica.model.Order;
import com.pruebatecnica.pruebatecnica.model.OrderItem;
import com.pruebatecnica.pruebatecnica.model.OrderStatus;
import com.pruebatecnica.pruebatecnica.model.Product;
import com.pruebatecnica.pruebatecnica.repository.OrderRepository;
import com.pruebatecnica.pruebatecnica.repository.ProductRepository;
import com.pruebatecnica.pruebatecnica.service.validator.StockValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Servicio de órdenes refactorizado siguiendo principios SOLID.
 * 
 * Principios aplicados:
 * - Single Responsibility: Cada clase tiene una única responsabilidad
 * - Open/Closed: Fácil de extender sin modificar código existente
 * - Dependency Inversion: Depende de abstracciones (servicios inyectados)
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StockValidator stockValidator;

    @Autowired
    private PriceCalculator priceCalculator;

    @Autowired
    private DiscountService discountService;

    @Autowired
    private StockManager stockManager;

    /**
     * Crea una nueva orden a partir del request.
     * 
     * Este método ahora es legible como lenguaje natural:
     * 1. Crea la orden base
     * 2. Procesa los items
     * 3. Calcula el total
     * 4. Aplica descuentos
     * 5. Guarda la orden
     * 
     * @param request Los datos de la orden
     * @return La orden creada y guardada
     */
    @Override
    @Transactional
    public Order createOrder(CreateOrderRequest request) {

        // Paso 1: Crear la orden base
        Order order = createBaseOrder(request);

        // Paso 2: Procesar los items de la orden
        OrderProcessingResult result = processOrderItems(request.getItems());

        // Paso 3: Calcular el total
        BigDecimal totalAmount = priceCalculator.calculateOrderTotal(result.getItemPrices());

        // Paso 4: Aplicar descuentos si corresponde
        totalAmount = discountService.applyVarietyDiscount(totalAmount, result.getUniqueProductIds());

        // Paso 5: Completar y guardar la orden
        return finalizeOrder(order, result.getOrderItems(), totalAmount);
    }

    /**
     * Crea la orden base con la información del cliente.
     */
    private Order createBaseOrder(CreateOrderRequest request) {
        return new Order(request.getCustomerName(), request.getCustomerEmail());
    }

    /**
     * Procesa todos los items de la orden.
     * Valida stock, reduce inventario y crea los items de orden.
     */
    private OrderProcessingResult processOrderItems(List<OrderItemRequest> itemRequests) {
        List<OrderItem> orderItems = new ArrayList<>();
        List<BigDecimal> itemPrices = new ArrayList<>();
        Set<Long> uniqueProductIds = new HashSet<>();

        for (OrderItemRequest itemRequest : itemRequests) {
            OrderItem orderItem = processOrderItem(itemRequest, itemPrices, uniqueProductIds);
            orderItems.add(orderItem);
        }

        return new OrderProcessingResult(orderItems, itemPrices, uniqueProductIds);
    }

    /**
     * Procesa un item individual de la orden.
     */
    private OrderItem processOrderItem(
            OrderItemRequest itemRequest,
            List<BigDecimal> itemPrices,
            Set<Long> uniqueProductIds) {

        // Buscar el producto
        Product product = findProduct(itemRequest.getProductId());

        // Validar disponibilidad de stock
        stockValidator.validateStockAvailability(product, itemRequest.getQuantity());

        // Reducir el stock
        stockManager.reduceStock(product, itemRequest.getQuantity());

        // Calcular precio del item
        BigDecimal itemPrice = priceCalculator.calculateItemPrice(product, itemRequest.getQuantity());
        itemPrices.add(itemPrice);

        // Trackear productos únicos para descuentos
        uniqueProductIds.add(product.getId());

        // Crear y retornar el item de orden
        return new OrderItem(product, itemRequest.getQuantity());
    }

    /**
     * Busca un producto por ID.
     */
    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    /**
     * Finaliza la orden asignando items, total y estado, luego la guarda.
     */
    private Order finalizeOrder(Order order, List<OrderItem> orderItems, BigDecimal totalAmount) {
        // Asociar items con la orden
        orderItems.forEach(item -> item.setOrder(order));

        // Configurar la orden
        order.setItems(orderItems);
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.CONFIRMED);

        // Guardar y retornar
        return orderRepository.save(order);
    }

    @Override
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new BadRequestException("Order not found: " + orderId));
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    /**
     * Clase interna para encapsular el resultado del procesamiento de items.
     *
     */
    private static class OrderProcessingResult {
        private final List<OrderItem> orderItems;
        private final List<BigDecimal> itemPrices;
        private final Set<Long> uniqueProductIds;

        public OrderProcessingResult(
                List<OrderItem> orderItems,
                List<BigDecimal> itemPrices,
                Set<Long> uniqueProductIds) {
            this.orderItems = orderItems;
            this.itemPrices = itemPrices;
            this.uniqueProductIds = uniqueProductIds;
        }

        public List<OrderItem> getOrderItems() {
            return orderItems;
        }

        public List<BigDecimal> getItemPrices() {
            return itemPrices;
        }

        public Set<Long> getUniqueProductIds() {
            return uniqueProductIds;
        }
    }
}