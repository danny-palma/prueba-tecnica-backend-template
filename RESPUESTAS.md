# Respuestas - Prueba Técnica Backend Developer

## 1. Escenario de Concurrencia (Black Friday) 🏃‍♂️

### Problema
Es Black Friday y el sistema recibe 50 pedidos por segundo del iPhone 15 que solo tiene 10 unidades en stock. El resultado es un inventario negativo (-5 unidades).

### Pregunta
¿Qué mecanismo de base de datos o de Spring Boot utilizarías para asegurar que nunca se venda más stock del que existe, asumiendo múltiples instancias de la API corriendo en paralelo?

### Tu Respuesta

Para resolver el problema de concurrencia en el escenario de Black Friday, implementaría bloqueo pesimista con SELECT FOR UPDATE en la consulta que verifica y actualiza el stock. Esto garantiza que solo una transacción pueda modificar el stock a la vez, evitando condiciones de carrera y asegurando que nunca se venda más stock del disponible. Esto funciona correctamente aunque existan  10, 50 o 100 instancias de la API, porque el control está en la base de datos, no en memoria.


## 2. Pregunta Trampa de Arquitectura 🎯

### Propuesta del Junior Developer
Configurar TODAS las relaciones JPA (`@OneToMany`, `@ManyToOne`) con `FetchType.EAGER` para:
- Traer toda la data en una sola consulta
- Evitar `LazyInitializationException`
- Mejorar el rendimiento

### Pregunta
¿Aceptarías este Pull Request? ¿Por qué sí o por qué no? ¿Qué impacto tendría con millones de registros?

### Tu Respuesta

No lo aceptaria porque EAGER no envia el N+1, solamente lo disimula a costa de trasferir datos innecesarrios, con millones de registros la aplicaci´pn se volveria lenta y pesada por consumo de memoria. Es mejor usar Fetch Joins especificos para cargar exactamente lo que necesitamos. 


## 3. Reflexiones Adicionales (Opcional) 💭

### Sobre el Refactoring Realizado
```
La decisión más importante fue separar las responsabilidades en clases cohesivas en lugar de simplemente dividir el método en partes más pequeñas. Esto permitió:
1. Testing aislado: Cada validador/service se prueba independientemente
2. Reutilización: StockValidator puede usarse en otros contextos
3. Mantenimiento: Cambiar la lógica de descuento requiere solo modificar DiscountService
4. Legibilidad: El método createOrder() ahora lee como lenguaje natural
```

### Patrones de Diseño Aplicados
```
1. Strategy Pattern - Para descuentos:
   - DiscountStrategy interface permite agregar nuevos tipos de descuentos sin modificar código existente
   - VarietyDiscountStrategy encapsula la lógica específica
2. Template Method implícito en OrderService:
   - El flujo principal está definido pero cada paso es delegado a componentes específicos
   - Permite variar implementaciones individuales
3. Dependency Injection:
   - Todas las dependencias se inyectan, facilitando testing y modularidad
```

### Posibles Mejoras Futuras
```
1. Resolución del Problema de Concurrencia:
   - Implementar bloqueo pesimista con @Lock(LockModeType.PESSIMISTIC_WRITE)
   - O usar SELECT FOR UPDATE en repositorios personalizados
2. DTO Projection para Serialización:
   - Crear OrderDTO para evitar la referencia circular actual
   - Usar @JsonManagedReference o @JsonIgnore mientras tanto
3. Event-Driven Architecture:
   - Emitir eventos OrderCreated, StockUpdated
   - Permite desacoplar lógica adicional (notificaciones, auditoría)
```