# Respuestas - Prueba Técnica Backend Developer

## 1. Escenario de Concurrencia (Black Friday) 🏃‍♂️

### Problema
Es Black Friday y el sistema recibe 50 pedidos por segundo del iPhone 15 que solo tiene 10 unidades en stock. El resultado es un inventario negativo (-5 unidades).

### Pregunta
¿Qué mecanismo de base de datos o de Spring Boot utilizarías para asegurar que nunca se venda más stock del que existe, asumiendo múltiples instancias de la API corriendo en paralelo?
Posibles enfoques a considerar:
- Transacciones y niveles de aislamiento
- Bloqueos (locks) en base de datos
- Bloqueos optimistas vs pesimistas
- Uso de @Version para Optimistic Locking
- SELECT FOR UPDATE
- Implementación de un sistema de colas
- Otros mecanismos...

### Tu Respuesta
Al revisar las opciones que se podrían aplicar, en efecto tenemos las siguientes: Optimistic Locking, Pessimistic Locking, Transacciones con aislamiento fuerte (SERIALIZABLE), Cola de pedidos (Kafka, RabbitMQ, etc.) entre otras, y tomando en cuenta todos los posibles enfoques a considerar que nos indican en el listado, aquí tengo la comparación:
-Optimistic Locking
Funciona usando un campo @Version en la entidad. Cada vez que se actualiza el stock, Hibernate verifica que la versión no haya cambiado. Si cambió (porque otra transacción ya actualizó), lanza una excepción (OptimisticLockException).
Ventajas: es escalable, no bloquea filas y tiene buen rendimiento cuando hay pocas colisiones.
Desventajas: en escenarios de alta concurrencia genera muchas excepciones y requiere lógica de reintentos en la aplicación.
Adecuación en Black Friday: poco adecuado, porque habría demasiados fallos y reintentos bajo carga extrema, por ello esta opción no sería recomendado en este caso.
-Pessimistic Locking
Funciona bloqueando la fila con SELECT FOR UPDATE o con @Lock(LockModeType.PESSIMISTIC_WRITE) hasta que la transacción termina.
Ventajas: garantiza que nunca se sobrevende, no requiere reintentos y es muy seguro.
Desventajas: reduce rendimiento porque las demás transacciones deben esperar.
Adecuación en Black Friday: la mejor opción, porque asegura consistencia absoluta en el stock aunque haya espera, por ello esta opción es la que veo que es LA MÁS RECOMENDADA EN ESTE CASO.
-Transacciones con aislamiento fuerte (SERIALIZABLE)
Este nivel de aislamiento fuerza que las transacciones se ejecuten como si fueran secuenciales.
Ventajas: asegura consistencia total y evita anomalías.
Desventajas: es el nivel más costoso en rendimiento, puede bloquear muchas operaciones y ralentizar el sistema.
Adecuación en Black Friday: no práctico, demasiado costoso para alto tráfico, por ello esta opción no sería recomendada en este caso.
-Cola de pedidos (Kafka, RabbitMQ, etc.)
Los pedidos se encolan y se procesan secuencialmente por un consumidor.
Ventajas: elimina la concurrencia en el stock y es muy seguro.
Desventajas: introduce mayor latencia, requiere infraestructura adicional y aumenta la complejidad de la arquitectura.
Adecuación en Black Friday: útil en arquitecturas grandes, pero más complejo de implementar rápidamente, por ello esta opción no sería recomendado en este caso.
CONCLUSIÓN Y DECISIÓN FINAL: 
En un escenario de Black Friday, la prioridad absoluta es la consistencia del stock (no vender más de lo que existe). El rendimiento puede sacrificarse un poco, porque los clientes prefieren esperar unos segundos antes que recibir errores o que el sistema sobrevenda. Por eso, entre todas las opciones, "Pessimistic Locking" es la mejor elección: bloquea la fila del producto mientras se actualiza el stock, evita sobreventa sin necesidad de reintentos y es más simple de implementar en nuestra arquitectura actual con Spring Boot + JPA. Por ello ya dejé hecho el ajuste tanto en ProductRepository.java como en ProductService.java en el método validateAndUpdateStock, para su revisión.


---
## 2. Pregunta Trampa de Arquitectura 🎯

### Propuesta del Junior Developer
Configurar TODAS las relaciones JPA (`@OneToMany`, `@ManyToOne`) con `FetchType.EAGER` para:
- Traer toda la data en una sola consulta
- Evitar `LazyInitializationException`
- Mejorar el rendimiento

### Pregunta
¿Aceptarías este Pull Request? ¿Por qué sí o por qué no? ¿Qué impacto tendría con millones de registros?
Considera estos puntos:
- Problema N+1 vs Carga excesiva de memoria
- Impacto en el rendimiento con grandes volúmenes de datos
- Alternativas mejores (DTO projection, fetch joins específicos, etc.)
- Cuándo usar EAGER vs LAZY
- Mejores prácticas para manejar LazyInitializationException

### Tu Respuesta
Yo NO aceptaría ese Pull request: Lo primero y principal es que se debe manejar una arquitectura limpia, eso implica cargar solo lo necesario, usar EAGER en todas las relaciones degradaría el rendimiento ya que cada vez que se consulte una entidad, JPA traerá todas sus relaciones, aunque no se necesiten, se pueden generar consultas enormes, con múltiples joins, que pueden ser muy costosas en memoria y tiempo y en escenarios con colecciones grandes (@OneToMany), se puede terminar cargando miles de registros innecesarios. Esto introduciría problemas de rendimiento, acoplamiento y carga innecesaria de datos.
En cuanto al  problema N+1 problem, precisamente hay riesgo de consultas en cascada, es decir, aunque EAGER evita el LazyInitializationException, puede provocar que Hibernate dispare múltiples queries adicionales para cargar sus relaciones.
Voy a ampliar mejor el "problema N+1".. este ocurre cuando al consultar una lista de entidades con relaciones, JPA/Hibernate ejecuta una consulta inicial para traer las entidades principales (por ejemplo, 10 órdenes) y luego dispara una consulta adicional por cada relación asociada (por ejemplo, los ítems de cada orden). Esto significa que en lugar de una sola consulta, se terminan ejecutando 1 + N consultas, lo que degrada el rendimiento de manera significativa. Aunque usar FetchType.EAGER puede parecer una solución, en realidad puede provocar este mismo problema al cargar relaciones en cascada y generar múltiples queries innecesarias.

Así que la solución correcta para evitar todo lo anterior, es mantener FetchType.LAZY por defecto y resolver LazyInitializationException con buenas prácticas:
- Servicios transaccionales.
- Queries con JOIN FETCH, con esto se decide cuándo y cómo cargar relaciones. Esto te da control, evita sobrecarga y mantiene el rendimiento.
- DTOs para vistas o DTO projection que es la técnica concreta para mapear directamente el resultado de una consulta a un DTO, en lugar de traer la entidad completa, es decir, desde la consulta ya traes solo lo que necesitas. Usar DTOs para controlar qué datos se devuelven, se evitan problemas de LazyInitializationException, ciclos infinitos en JSON, o sobrecarga de datos.

Con lo anterior:
- La capa de persistencia es más eficiente y flexible.
- La capa de aplicación decide qué datos necesita y construye consultas adecuadas.
- No se sacrifica rendimiento global por evitar un error mal manejado

Por las anteriores razones, no aceptaría el Pull Request, ya que la solución propuesta es un anti‑patrón y la práctica correcta es mantener LAZY por defecto y controlar la carga con transacciones, fetch joins o DTOs como indiqué anteriormente.

---

## 3. Reflexiones Adicionales (Opcional) 💭

### Sobre el Refactoring Realizado
```
[Opcional: Explica brevemente las decisiones más importantes que tomaste durante la refactorización]
### Tu Respuesta
Al revisar el método "createOrder" se observó que:
- Tenía mucha lógica mezclada en un solo método (validación de stock, creación de la orden, persistencia de ítems, cálculo de totales),
que obviamente viola los principios SOLID.
- Había dependencia directa de las entidades JPA en la capa de servicio.
- Había dificultad para probar y mantener el código, porque todo estaba acoplado.
Se tomó la decisión de dividir el método en servicios especializados, con el fin de lograr código limpio y una arquitectura más mantenible, ajustándose de la siguiente manera:
*Se crea el componente OrderValidator, el cual pertenece a la lógica de negocio, y su responsabilidad es la de validar reglas y datos de entrada (campos de la orden y campos de los items de la orden). Por lo tanto se ubicó en el paquete service y se creó el subpaquete service/validator para mantener orden.
*También se crea el componente OrderCalculator, el cual es un componente que encapsula reglas de negocio (en nuestro caso cálculo de totales, descuentos), también pertenece a la capa service, pero separado de los servicios transaccionales. Por lo tanto se ubicó en el paquete service y se creó el subpaquete service/business.
Con esto, las clases OrderValidator y OrderCalculator se integran naturalmente en la capa service, pero organizadas en subpaquetes para mantener un código limpio y modular.
*En ProductService se agregó el método "validateAndUpdateStock" para validar y actualizar el stock de productos correctamente.
*y Finalmente en OrderService el método "createOrder" se ajusta quedando mucho más limpio porque delega toda la lógica a los anteriores componentes, dejando que haga toda la orquestación: 1-valida request, 2-valida productos y stock, 3-crea la orden, 4-calculo de total y regla descuento, 5-persiste la orden.

Cuáles fueron los beneficios del refactor en cuanto a SOLID?:
- SRP (Single Responsibility Principle): cada clase tiene una responsabilidad clara.
- OCP (Open/Closed Principle): puedes extender validaciones o reglas de descuento sin modificar el núcleo.
- DIP (Dependency Inversion Principle): OrderService depende de abstracciones (OrderRepository, ProductService, etc.), no de detalles.
- Clean Code: el método createOrder ahora es corto, legible, mantenible y fácil de probar.
Cuáles fueron los beneficios del refactor de forma general en cuanto a la organización?:
- Claridad: cada clase tiene un lugar lógico dentro de la arquitectura.
- Escalabilidad: si mañana se tienen que agregar más validadores o calculadoras, ya se tienen carpetas dedicadas.
- Mantenibilidad: el OrderService queda limpio y solo orquesta, mientras que las reglas específicas viven en sus propios componentes.
Con esta refactorización, el código quedó modular, testeable y preparado para crecer sin convertirse en un “monolito” dentro de un solo método.

```

### Patrones de Diseño Aplicados
```
[Opcional: Menciona qué patrones de diseño utilizaste y por qué]
Ya habían algunos patrones de diseño existentes como:
- Repository Pattern: ya se tenía, porque ProductRepository y OrderRepository encapsulan el acceso a datos.
- Transaction Script: el método createOrder ya era un script transaccional que organiza la lógica de negocio dentro de una transacción.
- Separation of Concerns: ya estaba, porque la lógica de negocio está en el servicio, el acceso a datos en los repositorios y la exposición en los controladores.
Así que en la refactorización no apliqué nuevos patrones.
```

### Posibles Mejoras Futuras
```
[Opcional: ¿Qué otras mejoras implementarías si tuvieras más tiempo?]
```