# AGENTS.md - Guía para Agentes de Código

Este archivo contiene información esencial para agentes de código que trabajan en este repositorio Spring Boot.

## 🚀 Comandos Esenciales

### Build y Ejecución
```bash
# Compilar el proyecto
./gradlew build

# Ejecutar la aplicación
./gradlew bootRun

# Limpiar y compilar
./gradlew clean build
```

### Testing
```bash
# Ejecutar todos los tests
./gradlew test

# Ejecutar un test específico
./gradlew test --tests "com.pruebatecnica.pruebatecnica.service.OrderServiceTest"

# Ejecutar un método de test específico
./gradlew test --tests "com.pruebatecnica.pruebatecnica.service.OrderServiceTest.testCreateBasicOrder"

# Ver reporte de tests en HTML
# Los reportes se generan en: build/reports/tests/test/index.html
```

### Verificación de Calidad
```bash
# Verificar código (si hay configuración de linting)
./gradlew check

# Verificar dependencias actualizadas
./gradlew dependencyUpdates
```

## 🏗️ Arquitectura del Proyecto

### Estructura de Paquetes
```
com.pruebatecnica.pruebatecnica/
├── config/          # Configuración de Spring
├── controller/      # Endpoints REST
├── dto/            # Data Transfer Objects
├── exception/      # Excepciones personalizadas
├── model/          # Entidades JPA
├── repository/     # Interfaces Spring Data JPA
└── service/        # Lógica de negocio
```

### Configuración Principal
- **Java 21** con toolchain
- **Spring Boot 4.0.0**
- **Base de datos H2** en memoria
- **JUnit 5 + Mockito** para testing
- **Gradle** como build tool

## 📋 Estilo de Código y Convenciones

### Imports
- Organizar imports: `jakarta.*`, `org.springframework.*`, `java.*`, luego paquetes locales
- Evitar imports comodín (`*`)
- Imports estáticos solo para asserts y matchers

### Formato de Clases
```java
@Service  // Anotación primero
public class OrderService {
    
    @Autowired  // Inyección de dependencias
    private OrderRepository orderRepository;
    
    // Constantes primero (si existen)
    private static final BigDecimal DISCOUNT_RATE = BigDecimal.valueOf(0.10);
    
    // Métodos públicos primero, luego privados
    // Constructores después de campos
    
    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        // Implementación
    }
}
```

### Nomenclatura
- **Classes**: PascalCase (`OrderService`, `ProductRepository`)
- **Methods**: camelCase (`createOrder`, `getProductById`)
- **Variables**: camelCase (`totalAmount`, `customerId`)
- **Constants**: UPPER_SNAKE_CASE (`DISCOUNT_RATE`, `MAX_ITEMS`)
- **Packages**: lowercase con puntos (`com.pruebatecnica.pruebatecnica.service`)

### Manejo de Excepciones
```java
// Excepciones personalizadas para casos de negocio
throw new ProductNotFoundException(productId);
throw new InsufficientStockException(productName, requested, available);

// Validaciones con IllegalArgumentException
if (request.getCustomerName() == null || request.getCustomerName().trim().isEmpty()) {
    throw new IllegalArgumentException("Customer name is required");
}
```

### Transacciones y JPA
- Usar `@Transactional` en métodos de servicio que modifican datos
- Configurar relaciones con `FetchType.LAZY` por defecto
- Usar `@JoinColumn` con `nullable = false` para relaciones obligatorias
- Evitar `FetchType.EAGER` para prevenir problemas de rendimiento

## 🧪 Patrones de Testing

### Estructura de Tests
```java
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    
    @Mock
    private OrderRepository orderRepository;
    
    @InjectMocks
    private OrderService orderService;
    
    @Test
    void testScenario_ExpectedBehavior() {
        // Arrange - preparar mocks y datos de prueba
        // Act - ejecutar el método bajo prueba
        // Assert - verificar resultados
    }
}
```

### Mocking Best Practices
- Mockear dependencias externas (`@Mock`)
- Usar `when().thenReturn()` para configurar comportamiento
- Verificar interacciones con `verify()` solo cuando sea necesario
- Evitar mocks de clases finales o estáticas

## 🎯 Principios SOLID a Aplicar

### Single Responsibility Principle
- Separar validación, cálculo y persistencia en diferentes clases
- Cada método debe tener una única responsabilidad

### Dependency Inversion
- Inyectar dependencias a través de interfaces
- Usar `@Autowired` en constructor o campo (preferentemente constructor)

### Open/Closed Principle
- Diseñar clases que puedan extenderse sin modificarse
- Usar patrones como Strategy para lógica variable (descuentos)

## 🔍 Configuración de Base de Datos

### H2 Console
- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:testdb`
- Usuario: `sa`
- Contraseña: (vacía)

### Propiedades Importantes
```properties
spring.jpa.hibernate.ddl-auto=create-drop  # Recrear tablas en cada inicio
spring.jpa.show-sql=true                   # Mostrar SQL en consola
spring.jpa.properties.hibernate.format_sql=true  # Formatear SQL
```

## ⚠️ Consideraciones Especiales

### Problema de Concurrencia Identificado
- El método `OrderService.createOrder()` tiene un problema de concurrencia en la gestión de stock
- Se requiere implementar bloqueo optimista (`@Version`) o pesimista para evitar sobreventa

### Refactorización Pendiente
- Separar lógica de validación en clases validator
- Extraer cálculo de precios en servicio dedicado
- Implementar patrón Strategy para descuentos

### Testing Incompleto
- Los tests para la lógica de descuentos están pendientes de implementación
- Los tests existentes pueden fallar después de refactorización

## 🛠️ Herramientas de Desarrollo

### IDE Recommendations
- Configurar formatter según convenciones del proyecto
- Habilitar inspecciones de Spring Boot y JPA
- Configurar ejecución de tests con gradle

### Debugging
- Usar breakpoints en métodos de servicio
- Monitorear consultas SQL generadas
- Verificar estado de entidades JPA antes de persistir

---

**Nota**: Este proyecto es una prueba técnica enfocada en clean code y arquitectura. Priorizar la legibilidad y mantenibilidad sobre soluciones complejas.