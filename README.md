
##  **Patrones de Diseño Aplicados**

### **Strategy Pattern**
- **`DiscountStrategy`** interface para definir contratos de descuentos
- **`VarietyDiscountStrategy`** implementación concreta del descuento por variedad
- **Beneficio:** Facilita añadir nuevos tipos de descuentos sin modificar código existente (Open/Closed Principle)

### **Template Method Pattern** (implícito)
- **`OrderService.createOrder()`** define el esqueleto del algoritmo
- Cada paso del proceso es delegado a componentes especializados
- **Beneficio:** Estructura consistente con variabilidad controlada en cada fase

### **Dependency Injection Pattern**
- **Inyección de dependencias** via `@Autowired` en todos los servicios
- **Componentes desacoplados** que dependen de abstracciones
- **Beneficio:** Facilita testing y reutilización de componentes

### **Single Responsibility Principle** (aplicado a nivel de clase)
- Cada clase tiene una única responsabilidad bien definida
- **`OrderRequestValidator`**, **`StockValidator`**, **`PricingService`**, **`DiscountService`**
- **Beneficio:** Código más mantenible y fácil de probar unitariamente

