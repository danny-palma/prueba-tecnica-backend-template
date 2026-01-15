# Respuestas - Prueba Técnica Backend Developer

## 1. Escenario de Concurrencia (Black Friday)

### Problema
Es Black Friday y el sistema recibe 50 pedidos por segundo del iPhone 15 que solo tiene 10 unidades en stock. El resultado es un inventario negativo (-5 unidades).

### Pregunta
¿Qué mecanismo de base de datos o de Spring Boot utilizarías para asegurar que nunca se venda más stock del que existe, asumiendo múltiples instancias de la API corriendo en paralelo?

### Respuesta

Usaría **Pessimistic Locking** con `SELECT FOR UPDATE`. Básicamente le digo a la base de datos "bloquea esta fila mientras yo la estoy usando".

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT p FROM Product p WHERE p.id = :id")
Optional<Product> findByIdWithLock(@Param("id") Long id);
```

¿Por qué esta solución y no otra?

El problema aquí es que tenemos muchas instancias de la API compitiendo por el mismo producto al mismo tiempo. Si uso Optimistic Locking (con `@Version`), cada vez que haya un conflicto el usuario tendría que reintentar, y con 50 peticiones por segundo eso sería un desastre - la mayoría fallaría.

Con el bloqueo pesimista, cuando alguien está comprando el iPhone, los demás esperan su turno. Sí, es más lento, pero al menos garantizo que no vendo lo que no tengo.

También agregaría un constraint en la base de datos como red de seguridad:

```sql
ALTER TABLE products ADD CONSTRAINT stock_non_negative CHECK (stock >= 0);
```

Así, aunque haya un bug en el código, la BD nunca va a permitir stock negativo.

---

## 2. Pregunta Trampa de Arquitectura

### Propuesta del Junior Developer
Configurar TODAS las relaciones JPA con `FetchType.EAGER` para traer toda la data en una sola consulta y evitar el `LazyInitializationException`.

### Pregunta
¿Aceptarías este Pull Request?

### Respuesta

No, no lo aceptaría. Entiendo la intención del compañero, el `LazyInitializationException` es molesto, pero la solución que propone va a causar problemas peores.

El tema es así: imagina que tenemos 1000 órdenes en la base de datos. Si hago un `findAll()` con EAGER, no solo traigo las 1000 órdenes, sino también todos sus items, y los productos de cada item. Estamos hablando de cargar miles de objetos en memoria cuando tal vez solo necesitaba una lista con los nombres de los clientes.

Y lo peor es que EAGER no hace "una sola consulta" como uno pensaría. JPA muchas veces termina haciendo una consulta por cada relación, así que en vez de mejorar el rendimiento, lo empeoramos.

Lo que yo haría en cambio:

1. Dejar todo en LAZY (que es el default) y usar `JOIN FETCH` solo cuando necesite los datos:
```java
@Query("SELECT o FROM Order o JOIN FETCH o.items WHERE o.id = :id")
Optional<Order> findByIdWithItems(@Param("id") Long id);
```

2. Para el `LazyInitializationException`, asegurarme de que las consultas estén dentro de una transacción con `@Transactional`, o cargar los datos que necesito antes de salir del contexto de la sesión.

3. Si solo necesito algunos campos, usar DTOs en vez de traer la entidad completa.

En resumen: LAZY te da control, EAGER te lo quita. Con millones de registros, EAGER puede tumbar la aplicación.

---

## 3. Reflexiones Adicionales

### Sobre el Refactoring

Lo que hice fue separar el método `createOrder()` que hacía de todo en componentes más pequeños:

- `OrderInputValidator` - valida los datos que llegan
- `StockValidator` - verifica que haya stock
- `PriceCalculator` - hace las cuentas
- `DiscountService` - aplica los descuentos

Ahora el código principal se lee más fácil:
```java
inputValidator.validate(request);
Order order = createOrderFromRequest(request);
List<OrderItem> items = processOrderItems(request, order);
BigDecimal subtotal = priceCalculator.calculateOrderSubtotal(items);
BigDecimal total = discountService.applyDiscounts(subtotal, items);
```

### Qué mejoraría si tuviera más tiempo

1. Implementar el bloqueo pesimista que mencioné en la pregunta 1
2. Arreglar el problema del JSON infinito que tienen los endpoints (hay referencias circulares entre Order y OrderItem)
3. Agregar más tipos de descuento - el código ya está preparado para eso
4. Meter algo de caché para los productos que se consultan mucho
