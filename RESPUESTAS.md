# Respuestas - Prueba Técnica Backend Developer

## 1. Escenario de Concurrencia (Black Friday) 🏃‍♂️

### Problema
Es Black Friday y el sistema recibe 50 pedidos por segundo del iPhone 15 que solo tiene 10 unidades en stock. El resultado es un inventario negativo (-5 unidades).

### Pregunta
¿Qué mecanismo de base de datos o de Spring Boot utilizarías para asegurar que nunca se venda más stock del que existe, asumiendo múltiples instancias de la API corriendo en paralelo?

### Tu Respuesta
```
eligiria el uso de bloqueo de base de datos por version en la arquitectura actual
aunque si se logra cambiar la arquitectura utilizaria sistema de encolamiento asyncrono

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
eligiria el uso de bloqueo de base de datos por version en la arquitectura actual
aunque si se logra cambiar la arquitectura utilizaria sistema de encolamiento asyncrono
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
[Escribe aquí tu respuesta]
No aceptaria la pull request, ya que FetchType.EAGER realiza un carga de toda la data, al inicio no podra ver reflejado el rendimiento, pero cuando crece la cantida de datos, generara los problemas de rendimiento y escalabilidad

Considera estos puntos:
- Problema N+1 vs Carga excesiva de memoria
- Impacto en el rendimiento con grandes volúmenes de datos
- Alternativas mejores (DTO projection, fetch joins específicos, etc.)
- Cuándo usar EAGER vs LAZY
- Mejores prácticas para manejar LazyInitializationException

¿Aceptarías la propuesta? ¿Qué alternativas sugerirías?
```
No aceptaria la pull request, ya que FetchType.EAGER realiza un carga de toda la data, al inicio no podra ver reflejado el rendimiento, pero cuando crece la cantida de datos, generara los problemas de rendimiento y escalabilidad, una alternativa seria dividir la informacion en objetos o dto mas simples o pequeños y mantener la carga perezosa; 

---

## 3. Reflexiones Adicionales (Opcional) 💭

### Sobre el Refactoring Realizado
```
[Opcional: Explica brevemente las decisiones más importantes que tomaste durante la refactorización]
```
Durante el refactoring tome la decision de dividir el metodo en mas pequeños, con responsabilidad unica, implementando streams,  
### Patrones de Diseño Aplicados
```
[Opcional: Menciona qué patrones de diseño utilizaste y por qué]
```
No utilice ningun patron de diseño, ya que adicione la dependencias, pero al momento de compilar me generaba errores
### Posibles Mejoras Futuras
```
[Opcional: ¿Qué otras mejoras implementarías si tuvieras más tiempo?]
```
Implementacion restControllerAdvice para manejo de excepciones
Implementacion de exception mas personalizadas para el manejo de exceptiones
Creacion de clase utils para el manejo de errores en las validaciones de los request
Creacion de Binding results o validations constrain para validacion de request
Implementacion de records en los request de los controllers
Implementacion patrones Builder, para la creacion de los objetos mappers
Implementacion de Mappers para la generacion de objetos
Implementacion de contratos o interfaces de clases service
Implementacion de paquetes para organizar las capas de dominio, infraestructura y aplicacion
Implementacion de documentacion como swagger u otros
Implementacion de Logs y checks de salud de la aplicacion

