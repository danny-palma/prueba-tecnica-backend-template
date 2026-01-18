package com.pruebatecnica.pruebatecnica.service.calculator;

import com.pruebatecnica.pruebatecnica.model.Order;
import com.pruebatecnica.pruebatecnica.model.OrderItem;
import com.pruebatecnica.pruebatecnica.model.OrderStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;

@Service
public class OrderCalculatorService {

    public void calculateTotals(Order order) {

        
        try (PrintWriter writer = new PrintWriter("log-trace.txt")) {

        // 1. Calcular subtotal del pedido
        BigDecimal total = order.getItems().stream()
                .map(item -> item.getProduct().getPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

                
            writer.println("Iniciando cálculo...");
            writer.println("Valor total inicial: " + total);

        // 2. Identificar tipos de productos distintos
        Set<Long> uniqueProductIds = order.getItems().stream()
                .map(item -> item.getProduct().getId())
                .collect(Collectors.toSet());

        // 3. Regla del “Descuento por Variedad”
        // Más de 3 tipos distintos de productos → 10% de descuento
        if (uniqueProductIds.size() > 3) {
            BigDecimal discount = total.multiply(BigDecimal.valueOf(0.10));
            total = total.subtract(discount);
            writer.println("Descuento: " + discount);
            writer.println("Total final: " + total);
        }

            writer.println("Productos distintos: " + uniqueProductIds.size());
            writer.flush(); // opcional, porque close() lo hace igual

        // 4. Asignar total final y estado CONFIRMADO
        order.setTotalAmount(total);
        order.setStatus(OrderStatus.CONFIRMED);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
