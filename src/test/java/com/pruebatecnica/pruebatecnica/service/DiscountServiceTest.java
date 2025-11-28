package com.pruebatecnica.pruebatecnica.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class DiscountServiceTest {

    @InjectMocks
    private DiscountService discountService;

    @Test
    @DisplayName("Debe aplicar 10% de descuento cuando hay más de 3 tipos de productos diferentes")
    void shouldApplyDiscountWhenVarietyThresholdExceeded() {
        // Arrange
        // 4 tipos de productos diferentes (supera el umbral de 3)
        Set<Long> uniqueProductIds = Set.of(1L, 2L, 3L, 4L);
        BigDecimal totalAmount = new BigDecimal("100.00");

        // Act
        BigDecimal result = discountService.applyVarietyDiscount(totalAmount, uniqueProductIds);

        // Assert
        // El 10% de 100 es 10. Total esperado: 90.00
        BigDecimal expectedTotal = new BigDecimal("90.000"); // BigDecimal arithmetic might affect scale, checking
                                                             // matches

        // Usamos compareTo para ignorar diferencias menores en escala (ej: 90.00 vs
        // 90.000)
        assertEquals(0, expectedTotal.compareTo(result),
                "El total debería ser 90.00 después del 10% de descuento");
    }

    @Test
    @DisplayName("No debe aplicar descuento cuando hay 3 o menos tipos de productos diferentes")
    void shouldNotApplyDiscountWhenVarietyThresholdNotExceeded() {
        // Arrange
        // 3 tipos de productos diferentes (en el límite)
        Set<Long> uniqueProductIds = Set.of(1L, 2L, 3L);
        BigDecimal totalAmount = new BigDecimal("100.00");

        // Act
        BigDecimal result = discountService.applyVarietyDiscount(totalAmount, uniqueProductIds);

        // Assert
        assertEquals(totalAmount, result, "El total no debería cambiar");
    }

    @Test
    @DisplayName("No debe aplicar descuento cuando hay solo 1 tipo de producto, aunque sean muchas unidades")
    void shouldNotApplyDiscountForSingleProductType() {
        // Arrange
        Set<Long> uniqueProductIds = Set.of(1L);
        BigDecimal totalAmount = new BigDecimal("500.00");

        // Act
        BigDecimal result = discountService.applyVarietyDiscount(totalAmount, uniqueProductIds);

        // Assert
        assertEquals(totalAmount, result, "El total no debería cambiar para un solo tipo de producto");
    }
}
