# Respuestas - Prueba Técnica Backend Developer

## 1. Escenario de Concurrencia (Black Friday) 🏃‍♂️

### Problema
Es Black Friday y el sistema recibe 50 pedidos por segundo del iPhone 15 que solo tiene 10 unidades en stock. El resultado es un inventario negativo (-5 unidades).

### Pregunta
¿Qué mecanismo de base de datos o de Spring Boot utilizarías para asegurar que nunca se venda más stock del que existe, asumiendo múltiples instancias de la API corriendo en paralelo?

### Tu Respuesta
```
[Escribe aquí tu respuesta]

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

### Respuesta

**No.** Rechazaría el Pull Request bajo el siguiente analisis:

### ¿Por qué?

**Problema N+1:** Al usar EAGER, hibernate ejecutará una consulta inicial para obtener las entidades principales y luego N consultas adicionales para cargar las relaciones de cada registro. En un listado con relaciones, esto multiplicaria exponencialmente el tráfico hacia la base de datos.

**Uso ineficiente de memoria:** Afectaria negativamente el rendimiento al consultar grandes volumenes de datos ya que se cargarían en la RAM muchos objetos relacionados, y en la mayoria de los casos el FrontEnd ni siquiera va a mostrar en esa pantalla específica toda la informacion que trajo la consulta relacionada.

**Impacto en millones de registros:**
Esto ocasionaria que la aplicacion sea inusable por la lentitud o probablemente se quede pegada o genere un error de memoria en el servidor.

**Eager Vs Lazy:**

- Lazy debe usarse para la mayoria de los casos por que en arquitecturas escalables, es la opción más segura ya que garantiza que la aplicación solo consuma los recursos que necesita en cada consulta.


- Eager lo utilizaria únicamente para relaciones muy pequeñas y estáticas donde tengo la certeza absoluta de que la tabla no crecerá, o crecerá muy poco y que el dato relacionado se requiere el 100% de las veces.

**Mejores practicas para manejar LazyInitializationException:**

Esta excepción ocurre cuando se intenta acceder a una relación marcada como LAZY (colección o entidad) después de que la Sesión de Hibernate o el Contexto de Persistencia se han cerrado.

Para manejarla correctamente, las mejores prácticas son:

Contexto Transaccional: Ejecutar la lógica de acceso dentro de un método @Transactional, lo que mantiene la sesión abierta durante todo el proceso de negocio.

Uso de DTOs: Mapear la información necesaria a objetos DTO dentro de la capa de servicio. Esto asegura que los datos se capturen mientras la transacción está activa y se entreguen a la capa superior.

Consultas Dirigidas: Utilizar JOIN FETCH o @EntityGraph para inicializar las colecciones necesarias en una sola consulta.

Considera estos puntos:
- Problema N+1 vs Carga excesiva de memoria
- Impacto en el rendimiento con grandes volúmenes de datos
- Alternativas mejores (DTO projection, fetch joins específicos, etc.)
- Cuándo usar EAGER vs LAZY
- Mejores prácticas para manejar LazyInitializationException

¿Aceptarías la propuesta? 

**No.** Aceptaria la propuesta.

¿Qué alternativas sugerirías?

### Alternativas sugeridas:
- Mantener las relaciones como **LAZY** por defecto.
- Se pueden usar **DTO Projections**, ya que con los DTO son clases sencillas que sirven para recuperar solo los campos necesarios.
- En el repositorio se puede utilizar **JOIN FETCH** para consultas personalizadas donde se requiera aplicar filtros específicos sobre los datos relacionados, o **@EntityGraph** cuando el objetivo es cargar la relación de forma limpia y declarativa en una sola consulta SQL, evitando así el problema de las N+1 select.

---

## 3. Reflexiones Adicionales (Opcional) 💭

### Sobre el Refactoring Realizado
```
[Opcional: Explica brevemente las decisiones más importantes que tomaste durante la refactorización]
```

### Patrones de Diseño Aplicados
```
[Opcional: Menciona qué patrones de diseño utilizaste y por qué]
```

### Posibles Mejoras Futuras
```
[Opcional: ¿Qué otras mejoras implementarías si tuvieras más tiempo?]
```