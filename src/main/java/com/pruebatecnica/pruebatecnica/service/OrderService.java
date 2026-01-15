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
import com.pruebatecnica.pruebatecnica.service.pricing.DiscountService;
import com.pruebatecnica.pruebatecnica.service.pricing.PriceCalculator;
import com.pruebatecnica.pruebatecnica.service.validation.OrderInputValidator;
import com.pruebatecnica.pruebatecnica.service.validation.StockValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio principal para la gestión de pedidos.
 * Orquesta la creación de pedidos delegando responsabilidades a componentes especializados.
 */
@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderInputValidator inputValidator;

    @Autowired
    private StockValidator stockValidator;

    @Autowired
    private PriceCalculator priceCalculator;

    @Autowired
    private DiscountService discountService;

    /**
     * Crea un nuevo pedido siguiendo el flujo:
     * 1. Validar datos de entrada
     * 2. Procesar items (validar stock y actualizar inventario)
     * 3. Calcular subtotal
     * 4. Aplicar descuentos
     * 5. Guardar pedido
     */
    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        // 1. Validar datos de entrada
        inputValidator.validate(request);

        // 2. Crear pedido y procesar items
        Order order = createOrderFromRequest(request);
        List<OrderItem> items = processOrderItems(request, order);

        // 3. Calcular subtotal y 4. Aplicar descuentos
        BigDecimal subtotal = priceCalculator.calculateOrderSubtotal(items);
        BigDecimal total = discountService.applyDiscounts(subtotal, items);

        // 5. Configurar y guardar pedido
        order.setItems(items);
        order.setTotalAmount(total);
        order.setStatus(OrderStatus.CONFIRMED);

        return saveOrder(order);
    }

    /**
     * Crea una nueva instancia de Order con los datos del cliente.
     */
    private Order createOrderFromRequest(CreateOrderRequest request) {
        return new Order(request.getCustomerName(), request.getCustomerEmail());
    }

    /**
     * Procesa todos los items del pedido.
     */
    private List<OrderItem> processOrderItems(CreateOrderRequest request, Order order) {
        List<OrderItem> items = new ArrayList<>();

        for (OrderItemRequest itemRequest : request.getItems()) {
            OrderItem item = processOrderItem(itemRequest, order);
            items.add(item);
        }

        return items;
    }

    /**
     * Procesa un item individual: busca producto, valida stock y descuenta inventario.
     */
    private OrderItem processOrderItem(OrderItemRequest itemRequest, Order order) {
        Product product = findProduct(itemRequest.getProductId());

        stockValidator.validateStockAvailability(product, itemRequest.getQuantity());
        updateProductStock(product, itemRequest.getQuantity());

        return createOrderItem(product, itemRequest.getQuantity(), order);
    }

    /**
     * Busca un producto por su ID. Lanza excepción si no existe.
     */
    private Product findProduct(Long productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    /**
     * Descuenta la cantidad comprada del stock del producto.
     */
    private void updateProductStock(Product product, Integer quantity) {
        product.setStock(product.getStock() - quantity);
        productRepository.save(product);
    }

    /**
     * Crea un OrderItem asociado al pedido.
     */
    private OrderItem createOrderItem(Product product, Integer quantity, Order order) {
        OrderItem orderItem = new OrderItem(product, quantity);
        orderItem.setOrder(order);
        return orderItem;
    }

    /**
     * Persiste el pedido en la base de datos.
     */
    private Order saveOrder(Order order) {
        return orderRepository.save(order);
    }

    /**
     * Obtiene un pedido por su ID.
     */
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + orderId));
    }

    /**
     * Obtiene todos los pedidos.
     */
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
}
