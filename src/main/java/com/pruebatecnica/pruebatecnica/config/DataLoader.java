package com.pruebatecnica.pruebatecnica.config;

import com.pruebatecnica.pruebatecnica.model.Product;
import com.pruebatecnica.pruebatecnica.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Componente encargado de cargar datos iniciales en la base de datos al
 * arrancar la aplicación.
 * Útil para demostraciones y pruebas locales.
 */
@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private ProductRepository productRepository;

    /**
     * Se ejecuta automáticamente al inicio. Carga datos de ejemplo si no existen
     * productos.
     * 
     * @param args Argumentos pasados al iniciar la aplicación.
     * @throws Exception Si ocurre un error durante la carga de datos.
     */
    @Override
    public void run(String... args) throws Exception {
        // Cargar datos de ejemplo solo si la base de datos está vacía
        if (productRepository.count() == 0) {
            loadSampleData();
        }
    }

    /**
     * Persiste una lista predefinida de productos para inicializar el catálogo.
     */
    private void loadSampleData() {
        // Productos para la prueba técnica
        productRepository.save(new Product("Manzana", BigDecimal.valueOf(1.50), 100));
        productRepository.save(new Product("Pera", BigDecimal.valueOf(2.00), 80));
        productRepository.save(new Product("Uva", BigDecimal.valueOf(3.50), 60));
        productRepository.save(new Product("Sandia", BigDecimal.valueOf(5.00), 25));
        productRepository.save(new Product("iPhone 15", BigDecimal.valueOf(999.99), 10));
        productRepository.save(new Product("Laptop", BigDecimal.valueOf(800.00), 15));
        productRepository.save(new Product("Mouse", BigDecimal.valueOf(25.99), 50));
        productRepository.save(new Product("Teclado", BigDecimal.valueOf(45.00), 30));
        productRepository.save(new Product("Monitor", BigDecimal.valueOf(299.99), 20));
        productRepository.save(new Product("Audifonos", BigDecimal.valueOf(150.00), 40));

        System.out.println("Datos de ejemplo cargados: " + productRepository.count() + " productos");
    }
}