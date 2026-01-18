package com.pruebatecnica.pruebatecnica.service;

import com.pruebatecnica.pruebatecnica.dto.CreateOrderRequest;
import com.pruebatecnica.pruebatecnica.dto.OrderItemRequest;
import com.pruebatecnica.pruebatecnica.model.Order;
import com.pruebatecnica.pruebatecnica.model.OrderItem;
import com.pruebatecnica.pruebatecnica.model.Product;
import com.pruebatecnica.pruebatecnica.repository.OrderRepository;
import com.pruebatecnica.pruebatecnica.repository.ProductRepository;
import com.pruebatecnica.pruebatecnica.service.validator.OrderRequestValidator;
import com.pruebatecnica.pruebatecnica.service.validator.StockValidator;
import com.pruebatecnica.pruebatecnica.service.calculator.OrderCalculatorService;
import com.pruebatecnica.pruebatecnica.service.factory.OrderFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;  // para mock(), when(), doNothing(), any(), etc.
import org.mockito.Mockito;           // opcional, si quieres llamar Mockito.mock()


import java.io.PrintWriter;
import java.io.FileNotFoundException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StockValidator stockValidator;

    @Mock
    private OrderRequestValidator orderRequestValidator;

    @Mock
    private OrderCalculatorService orderCalculator;

    @Mock
    private OrderFactory orderFactory;

    @InjectMocks
    private OrderService orderService;

    /**
     * NOTA IMPORTANTE: Estos tests están incompletos intencionalmente.
     * Los candidatos deben:
     * 1. Completar los tests faltantes para la lógica del descuento
     * 2. Arreglar los tests que no funcionan debido a la refactorización
     * 3. Agregar más casos de prueba según sea necesario
     */

    @Test
    void testCreateOrderWithoutDiscount_ShouldNotApplyVarietyDiscount() {

        // Arrange: crear productos simulados
        Product apple = new Product("Apple", BigDecimal.valueOf(1.0), 10);
        apple.setId(1L);

        Product orange = new Product("Orange", BigDecimal.valueOf(1.5), 5);
        orange.setId(2L);

        Product pear = new Product("Pear", BigDecimal.valueOf(2.0), 8);
        pear.setId(3L);

        // Mock del repositorio de productos
        when(productRepository.findById(1L)).thenReturn(Optional.of(apple));
        when(productRepository.findById(2L)).thenReturn(Optional.of(orange));
        when(productRepository.findById(3L)).thenReturn(Optional.of(pear));

        // Mock de orden
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        // Validadores reales
        OrderRequestValidator orderRequestValidator = new OrderRequestValidator();

        // Mock StockValidator
        StockValidator stockValidator = Mockito.mock(StockValidator.class);
        doNothing().when(stockValidator).validateStock(any(CreateOrderRequest.class));

        // Mocks de factory y calculator
        OrderFactory orderFactory = Mockito.mock(OrderFactory.class);
        OrderCalculatorService orderCalculator = Mockito.mock(OrderCalculatorService.class);

        // IMPORTANTE: Mockear factory.buildOrder para devolver una orden real
        when(orderFactory.buildOrder(any(CreateOrderRequest.class)))
        .thenAnswer(inv -> {
            CreateOrderRequest req = inv.getArgument(0);

            // Crear una orden con sus items calculados directamente
            Order o = new Order();
            o.setCustomerName(req.getCustomerName());
            o.setCustomerEmail(req.getCustomerEmail());

            // Transformar los OrderItemRequest -> OrderItem manualmente
            List<OrderItem> items = req.getItems().stream()
                .map( it -> {
                    Product p = productRepository.findById(it.getProductId()).get();
                    return new OrderItem(p, it.getQuantity());
                })
                .toList();

            o.setItems(items);
            return o;
        });

        // Mockear calculator.calculateTotal
        doCallRealMethod().when(orderCalculator).calculateTotals(any(Order.class));
        // (Si tienes lógica real, mejor darle su implementación real o un cálculo equivalente)

        // Crear OrderService con todas las dependencias
        orderService = new OrderService(
            orderRepository,
            orderRequestValidator,
            stockValidator,
            orderFactory,
            orderCalculator
        );

        // Crear request con 3 tipos (NO aplica descuento)
        List<OrderItemRequest> items = List.of(
            new OrderItemRequest(1L, 2),
            new OrderItemRequest(2L, 1),
            new OrderItemRequest(3L, 3)
        );
        CreateOrderRequest request = new CreateOrderRequest("Jose David Gomez", "direccion@correo.com", items);

        // Act
        Order result = orderService.createOrder(request);

        // Assert: total esperado sin descuento
        BigDecimal expectedTotal =
            apple.getPrice().multiply(BigDecimal.valueOf(2))
            .add(orange.getPrice().multiply(BigDecimal.valueOf(1)))
            .add(pear.getPrice().multiply(BigDecimal.valueOf(3)));

        assertEquals(
            0,
            expectedTotal.compareTo(result.getTotalAmount()),
            "El total no debe incluir descuento cuando hay 3 o menos tipos de productos"
        );
    }


    @Test
    void testCreateOrderWithDiscount_ShouldApplyVarietyDiscount() {
        // TODO: Los candidatos deben implementar este test
        // Test para verificar que SÍ se aplica descuento cuando hay más de 3 tipos de productos diferentes
        // Arrange: crear productos simulados
        Product apple = new Product("Apple", BigDecimal.valueOf(1.0), 10);
        apple.setId(1L);
        Product orange = new Product("Orange", BigDecimal.valueOf(1.5), 5);
        orange.setId(2L);
        Product pear = new Product("Pear", BigDecimal.valueOf(2.0), 8);
        pear.setId(3L);
        Product banana = new Product("Banana", BigDecimal.valueOf(1.2), 12);
        banana.setId(4L);

        // -----------------------------
        // Mockear repositorios
        // -----------------------------
        when(productRepository.findById(1L)).thenReturn(Optional.of(apple));
        when(productRepository.findById(2L)).thenReturn(Optional.of(orange));
        when(productRepository.findById(3L)).thenReturn(Optional.of(pear));
        when(productRepository.findById(4L)).thenReturn(Optional.of(banana));

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // -----------------------------
        // Crear request con 4 tipos de productos
        // -----------------------------
        List<OrderItemRequest> items = List.of(
            new OrderItemRequest(1L, 2),
            new OrderItemRequest(2L, 1),
            new OrderItemRequest(3L, 3),
            new OrderItemRequest(4L, 2)
        );
        CreateOrderRequest request = new CreateOrderRequest("Jose David Gomez", "direccion@correo.com", items);

        // -----------------------------
        // Instanciar servicios reales / mocks según corresponda
        // -----------------------------
        // Validadores reales
        OrderRequestValidator orderRequestValidator = new OrderRequestValidator();
        StockValidator stockValidator = Mockito.mock(StockValidator.class);
        doNothing().when(stockValidator).validateStock(any(CreateOrderRequest.class));

        // Mocks de OrderFactory y OrderCalculatorService
        OrderFactory orderFactory = Mockito.mock(OrderFactory.class);
        OrderCalculatorService orderCalculator = Mockito.mock(OrderCalculatorService.class);

        // Preparar buildOrder para devolver un Order con items reales
        when(orderFactory.buildOrder(any(CreateOrderRequest.class))).thenAnswer(invocation -> {
            CreateOrderRequest req = invocation.getArgument(0);
            Order order = new Order();
            List<OrderItem> orderItems = req.getItems().stream().map(itemReq -> {
                OrderItem oi = new OrderItem();
                Product p = productRepository.findById(itemReq.getProductId()).orElseThrow();
                oi.setProduct(p);
                oi.setQuantity(itemReq.getQuantity());
                return oi;
            }).toList();
            order.setItems(orderItems);
            return order;
        });

        // Preparar calculateTotals para calcular total + descuento por variedad
        doAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            BigDecimal total = order.getItems().stream()
                    .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Aplicar 10% de descuento si hay más de 3 tipos de productos
            if (order.getItems().stream().map(oi -> oi.getProduct().getId()).distinct().count() > 3) {
                total = total.multiply(BigDecimal.valueOf(0.9));
            }

            order.setTotalAmount(total);
            return null;
        }).when(orderCalculator).calculateTotals(any(Order.class));

        // -----------------------------
        // Crear OrderService con todo
        // -----------------------------
        OrderService orderService = new OrderService(
            orderRepository,
            orderRequestValidator,
            stockValidator,
            orderFactory,
            orderCalculator
        );

        // -----------------------------
        // Act: crear la orden usando createOrder()
        // -----------------------------
        Order result = orderService.createOrder(request);

        // -----------------------------
        // Assert: total esperado con descuento
        // -----------------------------
        BigDecimal totalWithoutDiscount = apple.getPrice().multiply(BigDecimal.valueOf(2))
                                    .add(orange.getPrice().multiply(BigDecimal.valueOf(1)))
                                    .add(pear.getPrice().multiply(BigDecimal.valueOf(3)))
                                    .add(banana.getPrice().multiply(BigDecimal.valueOf(2)));

        BigDecimal expectedTotal = totalWithoutDiscount.multiply(BigDecimal.valueOf(0.9)); // 10% descuento

        assertEquals(0, expectedTotal.compareTo(result.getTotalAmount()),
                    "El total debe incluir descuento cuando hay más de 3 tipos de productos");   
    }

    @Test
    void testCreateOrderWithSameProductMultipleTimes_ShouldNotApplyDiscount() {
        // Arrange: crear un solo producto
        Product apple = new Product("Apple", BigDecimal.valueOf(1.0), 20);
        apple.setId(1L);

        // Mock ProductRepository
        when(productRepository.findById(1L)).thenReturn(Optional.of(apple));

        // Mock OrderRepository → devuelve la orden tal cual
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        // Validadores reales
        OrderRequestValidator orderRequestValidator = new OrderRequestValidator();

        // Mock StockValidator
        StockValidator stockValidator = mock(StockValidator.class);
        doNothing().when(stockValidator).validateStock(any(CreateOrderRequest.class));

        // Mock OrderFactory → construya una orden real
        OrderFactory orderFactory = mock(OrderFactory.class);
        when(orderFactory.buildOrder(any(CreateOrderRequest.class)))
            .thenAnswer(invocation -> {
                CreateOrderRequest req = invocation.getArgument(0);

                Order order = new Order();
                order.setCustomerName(req.getCustomerName());

                List<OrderItem> orderItems = req.getItems().stream().map(oiReq -> {
                    OrderItem oi = new OrderItem();
                    oi.setProduct(productRepository.findById(oiReq.getProductId()).get());
                    oi.setQuantity(oiReq.getQuantity());
                    return oi;
                }).toList();

                order.setItems(orderItems);
                return order;
            });

        // Mock OrderCalculator → calcula total real
        OrderCalculatorService orderCalculator = mock(OrderCalculatorService.class);
        doAnswer(invocation -> {
            Order o = invocation.getArgument(0);

            BigDecimal total = o.getItems().stream()
                .map(i -> i.getProduct().getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            o.setTotalAmount(total);
            return null;
        }).when(orderCalculator).calculateTotals(any(Order.class));

        // Construir OrderService
        orderService = new OrderService(
            orderRepository,
            orderRequestValidator,
            stockValidator,
            orderFactory,
            orderCalculator
        );

        // Request: 10 manzanas
        List<OrderItemRequest> items = List.of(new OrderItemRequest(1L, 10));
        CreateOrderRequest request = new CreateOrderRequest("Jose David Gomez", "direccion@correo.com", items);

        // Act
        Order result = orderService.createOrder(request);

        // Assert
        BigDecimal expectedTotal = apple.getPrice().multiply(BigDecimal.valueOf(10));

        assertEquals(0, expectedTotal.compareTo(result.getTotalAmount()),
            "El total no debe incluir descuento cuando solo hay un tipo de producto");
    }


    // Este test básico está roto intencionalmente debido al código monolítico
    @Test
    void testCreateBasicOrder() {
        // Arrange
        Product product1 = new Product("Test Product", BigDecimal.valueOf(10.00), 5);
        product1.setId(1L);

        // Validadores reales
        OrderRequestValidator orderRequestValidator = new OrderRequestValidator();
        StockValidator stockValidator = mock(StockValidator.class);
        doNothing().when(stockValidator).validateStock(any(CreateOrderRequest.class));

        // Mocks
        OrderFactory orderFactory = mock(OrderFactory.class);
        OrderCalculatorService orderCalculator = mock(OrderCalculatorService.class);

        // Repos
        when(productRepository.findById(1L)).thenReturn(Optional.of(product1));
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        // Mock OrderFactory → construya orden REAL
        when(orderFactory.buildOrder(any(CreateOrderRequest.class)))
            .thenAnswer(inv -> {
                CreateOrderRequest req = inv.getArgument(0);

                Order order = new Order();
                order.setCustomerName(req.getCustomerName());

                List<OrderItem> items = req.getItems().stream().map(r -> {
                    OrderItem oi = new OrderItem();
                    oi.setProduct(productRepository.findById(r.getProductId()).get());
                    oi.setQuantity(r.getQuantity());
                    return oi;
                }).toList();

                order.setItems(items);
                return order;
            });

        // Mock OrderCalculator → calcule total REAL
        doAnswer(inv -> {
            Order o = inv.getArgument(0);
            BigDecimal total = o.getItems().stream()
                .map(i -> i.getProduct().getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            o.setTotalAmount(total);
            return null;
        }).when(orderCalculator).calculateTotals(any(Order.class));

        // Request
        OrderItemRequest item = new OrderItemRequest(1L, 2);
        CreateOrderRequest request = new CreateOrderRequest("Jose David Gomez", "direccion@correo.com", List.of(item));

        // Crear servicio
        orderService = new OrderService(
            orderRepository,
            orderRequestValidator,
            stockValidator,
            orderFactory,
            orderCalculator
        );

        // Act
        Order result = orderService.createOrder(request);

        // Assert
        assertNotNull(result);
        assertEquals("Jose David Gomez", result.getCustomerName());

        BigDecimal expectedTotal = BigDecimal.valueOf(20.00);
        assertEquals(0, expectedTotal.compareTo(result.getTotalAmount()));
    }
}