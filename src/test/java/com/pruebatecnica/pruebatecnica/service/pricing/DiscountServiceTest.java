package com.pruebatecnica.pruebatecnica.service.pricing;

import com.pruebatecnica.pruebatecnica.model.OrderItem;
import com.pruebatecnica.pruebatecnica.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para la lógica de descuentos.
 * Verifica el descuento "Variedad": más de 3 tipos de productos = 10% descuento.
 */
class DiscountServiceTest {

    private DiscountService discountService;

    @BeforeEach
    void setUp() {
        discountService = new DiscountService();
    }

    // ==================== TESTS: NO APLICA DESCUENTO ====================

    @Test
    @DisplayName("NO descuento: 1 tipo de producto")
    void shouldNotApplyDiscount_WhenOnlyOneProductType() {
        // Arrange: 1 producto (Manzana)
        Product manzana = createProduct(1L, "Manzana", BigDecimal.valueOf(2.00));
        List<OrderItem> items = List.of(
            createOrderItem(manzana, 5)
        );
        BigDecimal subtotal = BigDecimal.valueOf(10.00);

        // Act
        BigDecimal total = discountService.applyDiscounts(subtotal, items);

        // Assert: Sin descuento, total = subtotal
        assertEquals(subtotal, total);
    }

    @Test
    @DisplayName("NO descuento: 2 tipos de productos")
    void shouldNotApplyDiscount_WhenTwoProductTypes() {
        // Arrange: 2 productos diferentes
        Product manzana = createProduct(1L, "Manzana", BigDecimal.valueOf(2.00));
        Product pera = createProduct(2L, "Pera", BigDecimal.valueOf(3.00));
        List<OrderItem> items = List.of(
            createOrderItem(manzana, 3),
            createOrderItem(pera, 2)
        );
        BigDecimal subtotal = BigDecimal.valueOf(12.00);

        // Act
        BigDecimal total = discountService.applyDiscounts(subtotal, items);

        // Assert: Sin descuento
        assertEquals(subtotal, total);
    }

    @Test
    @DisplayName("NO descuento: exactamente 3 tipos de productos")
    void shouldNotApplyDiscount_WhenExactlyThreeProductTypes() {
        // Arrange: 3 productos diferentes (límite, NO aplica)
        Product manzana = createProduct(1L, "Manzana", BigDecimal.valueOf(2.00));
        Product pera = createProduct(2L, "Pera", BigDecimal.valueOf(3.00));
        Product uva = createProduct(3L, "Uva", BigDecimal.valueOf(4.00));
        List<OrderItem> items = List.of(
            createOrderItem(manzana, 1),
            createOrderItem(pera, 1),
            createOrderItem(uva, 1)
        );
        BigDecimal subtotal = BigDecimal.valueOf(9.00);

        // Act
        BigDecimal total = discountService.applyDiscounts(subtotal, items);

        // Assert: Sin descuento (se requieren MÁS de 3)
        assertEquals(subtotal, total);
    }

    // ==================== TESTS: SÍ APLICA DESCUENTO ====================

    @Test
    @DisplayName("SÍ descuento: 4 tipos de productos = 10% descuento")
    void shouldApplyDiscount_WhenFourProductTypes() {
        // Arrange: 4 productos diferentes
        Product manzana = createProduct(1L, "Manzana", BigDecimal.valueOf(2.00));
        Product pera = createProduct(2L, "Pera", BigDecimal.valueOf(3.00));
        Product uva = createProduct(3L, "Uva", BigDecimal.valueOf(4.00));
        Product sandia = createProduct(4L, "Sandía", BigDecimal.valueOf(5.00));
        List<OrderItem> items = List.of(
            createOrderItem(manzana, 1),
            createOrderItem(pera, 1),
            createOrderItem(uva, 1),
            createOrderItem(sandia, 1)
        );
        BigDecimal subtotal = BigDecimal.valueOf(100.00);

        // Act
        BigDecimal total = discountService.applyDiscounts(subtotal, items);

        // Assert: 10% descuento -> 100 - 10 = 90
        BigDecimal expectedTotal = BigDecimal.valueOf(90.00);
        assertEquals(0, expectedTotal.compareTo(total));
    }

    @Test
    @DisplayName("SÍ descuento: 5 tipos de productos")
    void shouldApplyDiscount_WhenFiveProductTypes() {
        // Arrange: 5 productos diferentes
        Product p1 = createProduct(1L, "Producto 1", BigDecimal.valueOf(10.00));
        Product p2 = createProduct(2L, "Producto 2", BigDecimal.valueOf(10.00));
        Product p3 = createProduct(3L, "Producto 3", BigDecimal.valueOf(10.00));
        Product p4 = createProduct(4L, "Producto 4", BigDecimal.valueOf(10.00));
        Product p5 = createProduct(5L, "Producto 5", BigDecimal.valueOf(10.00));
        List<OrderItem> items = List.of(
            createOrderItem(p1, 1),
            createOrderItem(p2, 1),
            createOrderItem(p3, 1),
            createOrderItem(p4, 1),
            createOrderItem(p5, 1)
        );
        BigDecimal subtotal = BigDecimal.valueOf(50.00);

        // Act
        BigDecimal total = discountService.applyDiscounts(subtotal, items);

        // Assert: 10% descuento -> 50 - 5 = 45
        BigDecimal expectedTotal = BigDecimal.valueOf(45.00);
        assertEquals(0, expectedTotal.compareTo(total));
    }

    // ==================== TESTS: MÚLTIPLES UNIDADES DEL MISMO PRODUCTO ====================

    @Test
    @DisplayName("NO descuento: 10 unidades del mismo producto = 1 tipo")
    void shouldNotApplyDiscount_WhenMultipleUnitsOfSameProduct() {
        // Arrange: 10 Manzanas = solo 1 tipo de producto
        Product manzana = createProduct(1L, "Manzana", BigDecimal.valueOf(1.50));
        List<OrderItem> items = List.of(
            createOrderItem(manzana, 10)
        );
        BigDecimal subtotal = BigDecimal.valueOf(15.00);

        // Act
        BigDecimal total = discountService.applyDiscounts(subtotal, items);

        // Assert: Sin descuento (1 tipo, aunque sean 10 unidades)
        assertEquals(subtotal, total);
    }

    @Test
    @DisplayName("NO descuento: mismo producto en múltiples items = 1 tipo")
    void shouldNotApplyDiscount_WhenSameProductInMultipleItems() {
        // Arrange: Manzana aparece 3 veces, pero sigue siendo 1 tipo
        Product manzana = createProduct(1L, "Manzana", BigDecimal.valueOf(2.00));
        List<OrderItem> items = List.of(
            createOrderItem(manzana, 2),
            createOrderItem(manzana, 3),
            createOrderItem(manzana, 5)
        );
        BigDecimal subtotal = BigDecimal.valueOf(20.00);

        // Act
        BigDecimal total = discountService.applyDiscounts(subtotal, items);

        // Assert: Sin descuento (mismo producto repetido = 1 tipo)
        assertEquals(subtotal, total);
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Crea un producto de prueba con ID, nombre y precio.
     */
    private Product createProduct(Long id, String name, BigDecimal price) {
        Product product = new Product(name, price, 100);
        product.setId(id);
        return product;
    }

    /**
     * Crea un item de pedido con producto y cantidad.
     */
    private OrderItem createOrderItem(Product product, int quantity) {
        return new OrderItem(product, quantity);
    }
}
