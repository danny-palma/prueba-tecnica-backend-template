# Respuestas - Prueba Técnica Backend Developer

## 1. Escenario de Concurrencia (Black Friday) 🏃‍♂️

### Problema
Es Black Friday y el sistema recibe 50 pedidos por segundo del iPhone 15 que solo tiene 10 unidades en stock. El resultado es un inventario negativo (-5 unidades).

### Pregunta
¿Qué mecanismo de base de datos o de Spring Boot utilizarías para asegurar que nunca se venda más stock del que existe, asumiendo múltiples instancias de la API corriendo en paralelo?

### Tu Respuesta

Para garantizar que nunca se venda más stock del disponible en escenarios de alta concurrencia (como Black Friday), optaría por un **sistema de colas (arquitectura event-driven)** para desacoplar la creación del pedido del proceso de decremento del inventario. Esto evita condiciones de carrera entre múltiples instancias de la API y asegura que el stock solo se modifique de forma secuencial o controlada.

### Enfoque elegido: Sistema de Colas (Event-Driven Architecture)

En lugar de que cada instancia de la API intente modificar el inventario directamente en la base de datos (lo que genera contención y posibles overselling), implementaría:

1. **Product API**
   - Recibe la solicitud de compra.
   - Valida datos básicos (producto existe, usuario existe).
   - Publica un evento `OrderRequested` en una cola o tópico (Kafka / RabbitMQ / AWS SQS).
   - Devuelve una respuesta inmediata al cliente (“orden recibida, en proceso”).

2. **Inventory Service (worker/consumer)**
   - Un *solo consumidor* o un *consumer group con particionado por producto* procesa los eventos.
   - Cada mensaje se procesa **en orden** y de forma aislada.
   - Lógica del consumidor:
     ```
     - Leer evento OrderRequested.
     - Obtener stock actual del producto.
     - Validar disponibilidad.
     - Descontar stock.
     - Publicar evento OrderConfirmed o OrderRejected.
     ```
   - Como todos los descuentos se realizan desde un único flujo controlado, es imposible tener inventario negativo.

3. **Persistencia**
   - El worker opera dentro de una transacción corta y simple.
   - No requiere locks sofisticados, ya que el paralelismo está controlado por la cola.

4. **Flujo Completo**
API → OrderRequested → COLA → InventoryWorker → StockUpdated → (Order Service / Notifications)


### Por qué este enfoque evita el inventario negativo
- La cola **serializa** el acceso al recurso crítico (inventario).
- No importa cuántas instancias de la API haya, solo el worker controla la operación.
- Incluso con miles de mensajes/segundo, no hay condiciones de carrera.
- Es la arquitectura usada por Amazon, MercadoLibre, Rappi, etc., en sus eventos de alto tráfico.

### Aspectos técnicos clave de la implementación

#### Con Kafka:
- Crearía un tópico `orders.requested`.
- Particionado usando `productId` como *key*, para garantizar **orden por producto**.
- Un consumer group con N workers procesando en paralelo distintos productos, pero **nunca dos workers procesan el mismo producto**.
- El worker usa transacciones de Kafka para garantizar *exactly-once delivery*.

#### Con RabbitMQ:
- Usaría una cola dedicada o un **work queue**.
- Prefetch=1 para garantizar procesamiento ordenado.
- Dead Letter Queue para manejar pedidos fallidos.

#### Idempotencia
El consumidor implementaría un mecanismo idempotente:
- Cada mensaje contiene `orderId`.
- La operación de decremento verifica si esa orden ya fue procesada.
- Prevenir dobles actualizaciones por reentrega del mensaje.

#### Ventajas del enfoque
- Máxima escalabilidad horizontal.
- Sin bloqueos ni contención en la base de datos.
- A prueba de picos extremos de tráfico.
- Observabilidad fácil mediante logs y métricas del worker.
- Permite auditar toda la cadena de eventos.

### Conclusión
Implementaría un **sistema event-driven basado en colas** para gestionar la disminución del inventario, asegurando un único flujo controlado por producto.  
Este enfoque elimina totalmente la posibilidad de overselling, garantiza consistencia, y es la solución más robusta para escenarios reales de alto tráfico como Black Friday.

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
No aceptaría este Pull Request.

Configurar todas las relaciones JPA con FetchType.EAGER es una mala práctica de arquitectura porque genera más problemas de los que pretende resolver. Aunque suene conveniente “traer todo en una sola consulta”, en realidad tiene impactos críticos en rendimiento, escalabilidad y uso de memoria, especialmente cuando la aplicación crece o debe manejar millones de registros.

**1. Problema N+1 vs Carga excesiva**
Es cierto que el LAZY mal manejado puede causar el problema N+1, pero cambiar todas las relaciones a EAGER es pasar al extremo contrario:  
→ En vez de muchas consultas pequeñas, generas **una sola consulta gigante**, con múltiples joins, duplicación de datos y alto consumo de memoria.  
→ Es la diferencia entre “hacer 20 viajes en moto” y “cargar toda la casa en un solo camión pequeño y reventarlo”.

**2. Impacto con grandes volúmenes**
Con millones de registros el efecto es devastador:
- Tablas con relaciones EAGER producen queries inmanejables.
- El motor de BD debe procesar JOINs innecesarios.
- La capa de aplicación debe construir grafos completos aunque solo se necesite un campo.
- El servidor se queda sin memoria (OOM) o se degrada el GC.

En microservicios o APIs de alto volumen, un mal EAGER puede botar un clúster completo.

**3. Alternativas Correctas**
La solución no es “todo EAGER”; la solución es **controlar la carga según el caso de uso**:

- **DTO Projections** (Spring Data, queries nativas, JPQL select new)
- **Fetch joins específicos** solo cuando se necesitan
- **Entity Graphs** para escenarios puntuales
- **Lazy + transacciones bien delimitadas**
- **Cerrar correctamente el ciclo de vida** del persistence context

Con estas técnicas evitas N+1 sin comprometer memoria ni rendimiento.

**4. ¿Cuándo usar EAGER?**
Solo en relaciones *obligatorias, pequeñas y realmente necesarias* siempre:
- Catálogos pequeños
- Relaciones de 1:1 que son fundamentales para la entidad
- Configuración del dominio que no crece en tamaño

El 95% de los `@OneToMany` deberían ser LAZY.  
El 99% de los `@ManyToMany` deberían ser LAZY.  
Solo relaciones pequeñas y acotadas se justifican como EAGER.

**5. Manejo correcto del LazyInitializationException**
El error no se soluciona cambiando a EAGER; se soluciona con arquitectura:
- Mantener la transacción abierta durante el mapping (Open Session in View solo si es estrictamente necesario)
- Usar servicios transaccionales para inicializar lo que se necesita
- Técnicas como DTOs y fetch-joins bien diseñados

**Conclusión**
No aceptaría el PR porque aplicar EAGER globalmente es:
- Antipatrón
- No escalable
- Riesgoso con millones de registros
- Perjudicial para la performance

La recomendación es mantener LAZY por defecto y controlar la carga explícitamente según el caso de uso, con proyecciones o joins específicos.

---

## 3. Reflexiones Adicionales (Opcional) 💭

### Sobre el Refactoring Realizado
Durante la refactorización de los endpoints de órdenes, las decisiones clave se centraron en mejorar la cohesión, reducir duplicación y asegurar un flujo transaccional más consistente. 

Unifiqué la lógica dispersa en varios métodos para centralizar validaciones, cálculos y reglas de negocio en servicios especializados. Esto permitió eliminar código repetido entre los controladores y los tests, reduciendo el acoplamiento y facilitando la mantenibilidad.

También reorganizé la capa de persistencia para hacer los queries más explícitos, usando proyecciones y fetch-joins solo donde eran realmente necesarios. Esto redujo tiempos de respuesta y evitó problemas de carga excesiva de datos.

Finalmente, en los tests, migré varios casos aislados hacia pruebas más orientadas a comportamiento (BDD), eliminando mocks innecesarios y asegurando que cada prueba validara el flujo real y no solo la implementación.

### Patrones de Diseño Aplicados
Aplicaron principalmente los siguientes patrones:

- **Service Layer Pattern:** Para encapsular la lógica de negocio y evitar que los controladores gestionaran reglas complejas o procesos transaccionales.

- **DTO / Data Mapper Pattern:** Para desacoplar las entidades del modelo de datos del contrato REST, evitando exponer el modelo interno y permitiendo respuestas más ligeras.

- **Repository Pattern (Spring Data):** Para estandarizar el acceso a datos y delegar consultas complejas en métodos claramente definidos.

- **Specification / Query Object (cuando aplique):** Para construir consultas dinámicas sin mezclar lógica condicional dentro del repositorio.

- **Factory Method (en los tests y mapeos):** Para crear objetos consistentes y reutilizables, reduciendo la duplicación y mejorando la claridad de las pruebas.

Estos patrones ayudaron a organizar mejor el código, hacerlo más predecible y facilitar su evolución.

### Posibles Mejoras Futuras
Si dispusiera de más tiempo implementaría estas mejoras:

1. **Refinar más la separación entre dominio y transporte**, creando Value Objects donde aún se usan tipos primitivos que representan conceptos de negocio.

2. **Agregar validaciones de negocio más formales**, utilizando un enfoque basado en reglas o un motor de validación para reducir la lógica dispersa.

3. **Optimizar aún más las consultas**, aplicando entity graphs o proyecciones especializadas para escenarios de alto tráfico.

4. **Completar la suite de pruebas**, aumentando cobertura en casos borde y pruebas de carga para validar el rendimiento con altos volúmenes de órdenes.

5. **Introducir un Event-Driven Approach**, donde ciertas acciones (creación de orden, cambio de estado, cálculo de precios) puedan generar eventos internos que desacoplen módulos.

6. **Documentar decisiones arquitectónicas (ADR)** para dejar trazabilidad de los cambios claves, motivaciones y alternativas consideradas.

Estas mejoras consolidarían la arquitectura y prepararían la solución para escalar sin problemas.
