# Respuestas - Prueba Técnica Backend Developer

## 1. Escenario de Concurrencia (Black Friday) 🏃‍♂️

### Problema
Es Black Friday y el sistema recibe 50 pedidos por segundo del iPhone 15 que solo tiene 10 unidades en stock. El resultado es un inventario negativo (-5 unidades).

### Pregunta
¿Qué mecanismo de base de datos o de Spring Boot utilizarías para asegurar que nunca se venda más stock del que existe, asumiendo múltiples instancias de la API corriendo en paralelo?

### Tu Respuesta
```
Para asegurar la integridad del inventario en un escenario de alta concurrencia, elegiría una combinación de Bloqueo Pesimista (Pessimistic Locking) y una restricción a nivel de base de datos.

1. Bloqueo Pesimista (@Lock(LockModeType.PESSIMISTIC_WRITE)):
En el repositorio de productos, implementaría el método de búsqueda con un bloqueo pesimista. Esto se traduce en un 'SELECT ... FOR UPDATE' a nivel SQL.
¿Por qué? Porque en un escenario de altísima contención (50 req/s sobre un mismo registro), el bloqueo optimista (@Version) fallaría constantemente obligando a realizar múltiples reintentos (retries), lo que degradaría la experiencia del usuario y aumentaría la carga de CPU. El bloqueo pesimista garantiza que el primer hilo en leer el registro bloquee a los demás hasta que su transacción (y la deducción de stock) termine.

2. Restricción Check a nivel DB:
Añadiría una restricción 'CHECK (stock >= 0)' en la tabla de productos. Esto sirve como la "última línea de defensa" en caso de que algún proceso se salte la lógica de negocio, lanzando una excepción de integridad antes de permitir una venta inválida.

Alternativa para escalabilidad masiva: Si el sistema creciera a niveles donde la base de datos es el cuello de botella, implementaría un sistema de colas (RabbitMQ/Kafka) para procesar los pedidos de forma asíncrona o un bloqueo distribuido con Redis (Redlock).
```

---

## 2. Pregunta Trampa de Arquitectura 🎯

### Propuesta del Junior Developer
Configurar TODAS las relaciones JPA (`@OneToMany`, `@ManyToOne`) con `FetchType.EAGER` para:
- Traer toda la data en una sola consulta
- Evitar `LazyInitializationException`
- Mejorar el rendimiento

### Pregunta
¿Aceptarías este Pull Request? ¿Por qué sí o por qué no? ¿Qué impacto tendría con millones de registros?

### Tu Respuesta
```
NO aceptaría este Pull Request. De hecho, es una de las "antiprácticas" más peligrosas en JPA/Hibernate por las siguientes razones:

1. Impacto en el Rendimiento (Explosión de Memoria):
Con millones de registros, un simple 'findAll()' de una entidad principal intentaría cargar recursivamente miles de objetos relacionados en memoria. Esto causaría un consumo masivo de RAM y muy probablemente un 'OutOfMemoryError'.

2. Problema del Producto Cartesiano:
Al hacer EAGER de múltiples colecciones, Hibernate genera JOINs complejos que multiplican las filas devueltas por la base de datos, degradando el tiempo de respuesta de la consulta SQL de forma exponencial.

3. Falta de Flexibilidad y Problema N+1:
EAGER obliga a traer siempre toda la data, incluso cuando solo necesitamos un campo básico. Esto desperdicia ancho de banda y recursos de IO de la base de datos.

Sugerencia de alternativas:
- Usar FetchType.LAZY siempre por defecto.
- Para evitar 'LazyInitializationException', asegurarse de que el acceso a los datos ocurre dentro de una sesión abierta (usando @Transactional en la capa de servicio).
- Para optimizar consultas específicas que sí requieren datos relacionados, usar 'Entity Graphs' o queries con 'JOIN FETCH'.
- Utilizar Proyecciones DTO para recuperar únicamente los campos necesarios para la vista/respuesta, lo cual es mucho más eficiente que traer entidades completas.
```

---

## 3. Reflexiones Adicionales (Opcional) 💭

### Sobre el Refactoring Realizado
```
Mi decisión principal fue descomponer el método 'createOrder' que era un monolito. Apliqué el Principio de Responsabilidad Única (SRP) creando servicios especializados:
- OrderValidator: Solo valida datos de entrada.
- InventoryService: Gestiona la integridad y persistencia del stock.
- PriceCalculator: Centraliza las reglas matemáticas y de negocio (descuentos).
Esto convirtió a OrderService en un orquestador limpio y fácil de leer.
```

### Patrones de Diseño Aplicados
```
- Patrón Estrategia (Strategy): Aunque simplificado en PriceCalculator, la lógica de descuentos está preparada para extenderse a diferentes estrategias de precio sin afectar al flujo principal (OCP).
- Patrón Fachada/Orquestador: El OrderService actúa como un punto de entrada que simplifica la interacción con múltiples subsistemas complejos.
```

### Posibles Mejoras Futuras
```
- Manejo Global de Excepciones: Implementar un @ControllerAdvice para devolver respuestas JSON estructuradas en lugar de errores crudos.
- Auditoría: Usar Spring Data Envers o @CreatedDate para trackear cambios en el stock y las órdenes.
- Caché: Implementar Redis para el catálogo de productos, ya que las lecturas suelen ser mucho más frecuentes que las actualizaciones de stock.
```