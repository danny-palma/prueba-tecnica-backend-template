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

Explica cuál elegirías y por qué.

### Respuesta
```

Para este escenario, utilizaría Pessimistic Locking (Bloqueo Pesimista) gestionado dentro de una Transacción de Spring.

Esto garantiza la integridad del sistema mediante:

- Atomicidad (vía @Transactional):
 Garantiza que todo el flujo (Leer, Validar, Descontar y Guardar) sea una operación única e indivisible. 
 Si ocurre cualquier error o el stock es insuficiente, la transacción hace un rollback automático y el estado de la base de datos permanece intacto.

- Bloqueo Pesimista (PESSIMISTIC_WRITE):
 Implementado mediante @Lock(LockModeType.PESSIMISTIC_WRITE) en el repositorio. 
 Este mecanismo bloquea la fila del producto en la base de datos (haciendo un SELECT FOR UPDATE) desde el inicio de la transacción.

- Gestión de Concurrencia:
 Al bloquear la fila, obligamos a las múltiples instancias de la API a procesar los pedidos de forma secuencial. 
 Ninguna otra instancia podrá leer o modificar el stock de ese producto hasta que la transacción actual termine, evitando así inconsistencias en el inventario.

```
**Implementación de un sistema de colas**
```
Es una solución más escalable, pero añade mucha complejidad. 
Se necesitará instalar un servidor de mensajería (Broker), manejar mensajes fallidos y la respuesta al usuario ya no es instantánea.
```


***Optimistic Locking***
```
El @Version es excelente para sistemas con poca colisión (donde es raro que dos personas compren lo mismo al mismo tiempo). 
Pero en un Black Friday con 50 pedidos por segundo sobre 10 iPhones, es probable que muchas de las transacciones fallen por error de versión.
```
## 2. Pregunta Trampa de Arquitectura 🎯

### Propuesta del Junior Developer
Configurar TODAS las relaciones JPA (`@OneToMany`, `@ManyToOne`) con `FetchType.EAGER` para:
- Traer toda la data en una sola consulta
- Evitar `LazyInitializationException`
- Mejorar el rendimiento

### Pregunta
¿Aceptarías este Pull Request? ¿Por qué sí o por qué no? ¿Qué impacto tendría con millones de registros?

### Respuesta
```
"No Aceptaria el Pull Request."
```
### ¿Por qué?

Considera estos puntos:
- Problema N+1 vs Carga excesiva de memoria
- Impacto en el rendimiento con grandes volúmenes de datos
- Alternativas mejores (DTO projection, fetch joins específicos, etc.)
- Cuándo usar EAGER vs LAZY
- Mejores prácticas para manejar LazyInitializationException


**Problema N+1 y Rendimiento:** 

```
- Al usar EAGER:
 hibernate ejecutará una consulta inicial para obtener las entidades principales y luego N consultas adicionales para cargar las relaciones de cada registro. 
 En un listado con relaciones, esto multiplicaria exponencialmente el tráfico hacia la base de datos.

- Uso ineficiente de memoria: 
Afectaria negativamente el rendimiento al consultar grandes volumenes de datos ya que se cargarían en la RAM muchos objetos relacionados, 
y en la mayoria de los casos el FrontEnd ni siquiera va a mostrar en esa pantalla específica toda la informacion que trajo la consulta relacionada.

- Impacto en millones de registros:
Esto ocasionaria que la aplicacion sea inusable por la lentitud o probablemente se quede pegada o genere un error de memoria en el servidor.
```

**Eager Vs Lazy:**
```
 - Lazy debe usarse para la mayoria de los casos por que en arquitecturas escalables, es la opción más segura ya que garantiza que la aplicación solo consuma los recursos que necesita en cada consulta.


 - Eager lo utilizaria únicamente para relaciones muy pequeñas y estáticas donde tengo la certeza absoluta de que la tabla no crecerá, o crecerá muy poco y que el dato relacionado se requiere el 100% de las veces.
```

**Mejores prácticas para manejar LazyInitializationException y alternativas mejores:**
```
Esta excepción ocurre cuando se intenta acceder a una relación marcada como LAZY (colección o entidad) después de que la Sesión de Hibernate o el Contexto de Persistencia se han cerrado.

Para manejarla correctamente, las mejores prácticas son:

- Contexto Transaccional: 
Ejecutar la lógica de acceso dentro de un método @Transactional, lo que mantiene la sesión abierta durante todo el proceso de negocio.

- Uso de DTOs:
 Mapear la información necesaria a objetos DTO dentro de la capa de servicio. 
 Esto asegura que los datos se capturen mientras la transacción está activa y se entreguen a la capa superior.

- Consultas Dirigidas:
 Utilizar JOIN FETCH o @EntityGraph para inicializar las colecciones necesarias en una sola consulta.
```

¿Aceptarías la propuesta? 
```
"No aceptaria la propuesta."
```
¿Qué alternativas sugerirías?

```
- Mantener las relaciones como **LAZY** por defecto.
- Se pueden usar **DTO Projections**, ya que con los DTO son clases sencillas que sirven para recuperar solo los campos necesarios.
- En el repositorio se puede utilizar **JOIN FETCH** para consultas personalizadas donde se requiera aplicar filtros específicos sobre los datos relacionados, 
o **@EntityGraph** cuando el objetivo es cargar la relación de forma limpia y declarativa en una sola consulta SQL, evitando así el problema de las N+1 select.
```
---

## 3. Reflexiones Adicionales (Opcional) 💭

### Sobre el Refactoring Realizado
```
Las decisiones principales se centraron en la Separación de Responsabilidades (SRP). 
Delegué la lógica de persistencia exclusivamente al repositorio y la lógica de negocio al servicio. 
Implementé un Manejo Global de Excepciones para eliminar los bloques try-catch repetitivos en los controladores, logrando un código más limpio y legible. 
Optimicé las consultas a la base de datos para asegurar que las operaciones de actualización de stock sean atómicas y consistentes
```

### Patrones de Diseño Aplicados
```
Utilicé los siguientes patrones para asegurar un diseño robusto:

 - Repository Pattern: 
 Para desacoplar la lógica de negocio de la tecnología de persistencia (JPA/Hibernate).

 - Service Layer: 
 Para centralizar la lógica de negocio y gestionar la transaccionalidad.

 - Dependency Injection (DI): 
 Implementado a través de Spring para facilitar el desacoplamiento y la testabilidad (unit testing con Mocks).

 - Data Transfer Object (DTO): 
 Para evitar exponer las entidades de la base de datos directamente a la API, protegiendo la integridad del modelo interno.
 
 - Programación Orientada a Aspectos (AOP):
 Con las excepciones globales, se capturaron errores para dar respuestas centralizadas y personalizadas al cliente.
```

### Posibles Mejoras Futuras
```
Implementaria las siguientes mejoras

1. Seguridad: 
Implementar OAuth2/JWT para proteger los endpoints y gestionar roles. 

2. Caché Distribuido (Redis): 
Es una tecnología que he manejado poco pero me interesa profundizar, especialmente para optimizar la consulta de productos de alta rotación y proteger la base de datos. 

3. Observabilidad y Monitoreo
Hasta ahora mi enfoque principal ha sido la lógica de desarrollo y la robustez del código. 
Sin embargo, como mejora futura, me gustaría integrar sistemas de observabilidad y monitoreo utilizando herramientas como Prometheus y Grafana;
para analizar tiempo de respuesta de los endpoints y numero de conexiones activas en DB.
```