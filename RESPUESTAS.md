# Respuestas - Prueba Técnica Backend Developer

## 1. Escenario de Concurrencia (Black Friday) 🏃‍♂️

### Problema
Es Black Friday y el sistema recibe 50 pedidos por segundo del iPhone 15 que solo tiene 10 unidades en stock. El resultado es un inventario negativo (-5 unidades).

### Pregunta
¿Qué mecanismo de base de datos o de Spring Boot utilizarías para asegurar que nunca se venda más stock del que existe, asumiendo múltiples instancias de la API corriendo en paralelo?

### Tu Respuesta
```
Solución Recomendada: Optimistic Locking con @Version
Para este escenario de Black Friday elegiria Optimistic Locking como mecanismo principal:

¿Cómo funciona?

- Agregar campo @Version en la entidad Product
- JPA incrementa automáticamente la versión en cada actualización
- Si dos transacciones intentan modificar el mismo registro, la segunda falla con OptimisticLockingFailureException
- Implementar reintentos automáticos con @Retryable (máximo 3 intentos)

¿Por qué esta solución?

- Performance superior: No bloquea lecturas, múltiples usuarios pueden consultar simultáneamente
- Escalable: Funciona bien con múltiples instancias de la API
- Simple de implementar: JPA lo maneja automáticamente
- Adecuado para el escenario: 50 req/s no requiere soluciones más complejas

Capas adicionales de seguridad:

- Validación explícita antes de decrementar, Verificar ANTES de restar que haya stock suficiente:
    if (product.getStock() < quantity) {
        throw new InsufficientStockException();
    }
    product.setStock(product.getStock() - quantity);
- Constraint en BD: CHECK (stock >= 0) como red de seguridad
- Transacción con aislamiento READ_COMMITTED

Alternativa si la concurrencia fuera EXTREMA (1000+ req/s):
Pessimistic Locking con SELECT FOR UPDATE:

- Bloquea el registro mientras se procesa
- Mayor consistencia pero peor rendimiento

O Cola asíncrona con Redis:

Pre-validación de stock en cache (operación atómica DECR)
Procesamiento asíncrono del pedido
Mejor UX y escalabilidad

Mi elección: Optimistic Locking + Constraint DB
Es el balance perfecto entre performance, consistencia y simplicidad para 50 req/s. Solo migraría a soluciones más complejas si el rendimiento aumentara significativamente.

---------------------------------------------

Posibles enfoques a considerar:
- Transacciones y niveles de aislamiento
- Bloqueos (locks) en base de datos
- Bloqueos optimistas vs pesimistas
- Uso de @Version para Optimistic Locking
- SELECT FOR UPDATE
- Implementación de un sistema de colas
- Otros mecanismos...

Explica cuál elegirías y por qué.
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
¿Aceptaría el Pull Request? NO.

¿Por qué es una mala idea?

1. Problema de Carga Cartesiana (Explosión de datos):
Si un Cliente tiene 1000 Pedidos y cada Pedido tiene 50 Items, traer un cliente carga 50,000 registros innecesariamente
Con millones de registros, esto causa OutOfMemoryError

2. Rendimiento degradado:
EAGER hace JOINs automáticos en CADA consulta, aunque no necesites esos datos
Consulta simple como findById(clienteId) se convierte en un query gigante con múltiples JOINs
Tiempo de respuesta de milisegundos pasa a segundos o minutos

3. Desperdicio de recursos:
Cargas datos que tal vez nunca uses en ese contexto
Aumenta consumo de memoria, CPU y ancho de banda de red

4. No resuelve el problema raíz:
LazyInitializationException indica mal diseño de transacciones, no un problema de FetchType
EAGER solo oculta el problema, no lo soluciona

Alternativas Correctas:

1. Mantener LAZY como default + Fetch Joins estratégicos:
@Query("SELECT c FROM Cliente c JOIN FETCH c.pedidos WHERE c.id = :id")
Cliente findByIdWithPedidos(@Param("id") Long id);

Cargas relaciones solo cuando las necesitas
Control granular por caso de uso

2. DTOs con Projections:
@Query("SELECT new com.example.ClienteDTO(c.id, c.nombre, COUNT(p)) " +
       "FROM Cliente c LEFT JOIN c.pedidos p GROUP BY c.id")
List<ClienteDTO> findClientesConContador();

Traes exactamente los datos necesarios
Mejor performance que entidades completas

3. @EntityGraph (Spring Data JPA):
@EntityGraph(attributePaths = {"pedidos", "pedidos.items"})
Cliente findByIdWithDetails(Long id);

Define qué relaciones cargar por método
Más declarativo y limpio

4. Resolver LazyInitializationException correctamente:

@Transactional en el service donde accedes a las relaciones lazy
O usar Open Session in View (con cuidado en producción)

Cuándo usar EAGER vs LAZY:
EAGER (casos muy específicos):

Relaciones que SIEMPRE necesitas (ej: Usuario → Perfil 1:1)
Datos pequeños y no escalables (ej: catálogos con 10-20 registros)

LAZY (regla general):

Todas las colecciones (@OneToMany, @ManyToMany)
Relaciones que pueden crecer indefinidamente
Default para el 95% de los casos

Mi respuesta al Junior:
Entiendo que quieres evitar LazyInitializationException, pero EAGER en todas las relaciones causaría problemas serios de performance. En lugar de eso, usemos LAZY como default y carguemos relaciones específicas con fetch joins o DTOs según el caso de uso.

---------------------------------------------

Considera estos puntos:
- Problema N+1 vs Carga excesiva de memoria
- Impacto en el rendimiento con grandes volúmenes de datos
- Alternativas mejores (DTO projection, fetch joins específicos, etc.)
- Cuándo usar EAGER vs LAZY
- Mejores prácticas para manejar LazyInitializationException

¿Aceptarías la propuesta? ¿Qué alternativas sugerirías?
```

---

## 3. Reflexiones Adicionales (Opcional) 💭

### Sobre el Refactoring Realizado
```
[Opcional: Explica brevemente las decisiones más importantes que tomaste durante la refactorización]

- Implementación de Arquitectura de Interfaces: Se separó la definición de los servicios (`OrderService`, `ProductService`) de su implementación lógica (`ServiceImpl`) para mejorar el desacoplamiento.
- Separación por Responsabilidad Única (SRP): Se crearon componentes especializados para stock (`StockManager`), precios (`PriceCalculator`) y descuentos (`DiscountService`), haciendo que el flujo principal de creación de órdenes sea legible y fácil de mantener.
- Manejo Global de Excepciones: Se agregó un `@RestControllerAdvice` para centralizar el manejo de errores.
- Validación Declarativa (Bean Validation): Se migraron las validaciones de campos obligatorios y formatos (email, cantidades mínimas) directamente a los DTOs usando anotaciones.
- Eliminación de Validaciones Manuales: Se limpió la lógica de los servicios eliminando chequeos manuales de campos nulos o vacíos, delegando esta responsabilidad a Spring Validation en la capa del controlador.
- Respuestas de Validación Mejoradas: Las validaciones de entrada ahora devuelven mensajes de error estructurados y específicos por campo, facilitando la depuración y la experiencia del consumidor de la API. Por ejemplo, ante una solicitud con datos inválidos:
//Solicitud:
{
  "customerName": "Ana García",
  "customerEmail": "ana@email.com",
  "items": [
    {"productId": 1, "quantity": 0},
    {"productId": null, "quantity": 2},
    {"productId": 3, "quantity": 1}
  ]
}
//Respuesta:
[
    {
        "field": "items[0].quantity",
        "message": "Quantity must be at least 1"
    },
    {
        "field": "items[1].productId",
        "message": "Product ID is required"
    }
]
```

### Patrones de Diseño Aplicados
```
[Opcional: Menciona qué patrones de diseño utilizaste y por qué]

- Strategy Pattern: Aplicado en `DiscountService` para encapsular la lógica de descuentos (como el de Variedad), permitiendo agregar nuevas reglas sin afectar al resto del sistema.
- Facade / Orchestrator: `OrderService` actúa como un orquestador de alto nivel que delega tareas complejas a componentes especializados, simplificando la interfaz para el controlador.
- Dependency Injection (DI): Utilizado para desacoplar los servicios de sus implementaciones, facilitando el mantenimiento y las pruebas unitarias.
- Data Transfer Object (DTO): Implementado para separar el modelo de dominio de los datos de entrada/salida de la API, protegiendo la integridad de la base de datos.
- Singleton: Patrón nativo de Spring utilizado para los servicios, asegurando eficiencia en el uso de memoria.


[Opcional: ¿Qué otras mejoras implementarías si tuvieras más tiempo?]

- SonarQube: Implementar `SonarQube` para mejorar la calidad del código, detectando duplicaciones y vulnerabilidades. **Actualmente existe un problema de compatibilidad entre el plugin de SonarQube y Gradle 9.2.1 (la API getConvention() fue eliminada), por lo que se requiere usar Gradle 8.x temporalmente hasta que se lance una versión compatible del plugin.**
- DTOs: Definir objetos de transferencia de datos específicos para cada endpoint, en lugar de exponer entidades directamente. Esto mejora la seguridad, el rendimiento y la claridad de la API. Sería útil confirmar qué campos exactos necesita el frontend en cada respuesta para diseñar los DTOs adecuados.
- Paginación y Filtrado: Implementar `Pageable` de Spring Data en los endpoints de listado para manejar grandes volúmenes de datos de forma eficiente.
- Seguridad (Spring Security + JWT): Implementar autenticación y roles para proteger los endpoints sensibles.
- Documentación con Swagger: Configurar la generación automática de documentación interactiva para facilitar el consumo de la API para el desarrollo.




