# Especificación 001: Registrar una incidencia

## Estado

Revisada y lista para implementación.

## Objetivo

Permitir que un usuario registre una incidencia de software mediante una API REST.
La incidencia se creará con el estado inicial `OPEN`.

## Alcance

Esta especificación incluye únicamente la creación de incidencias.

No incluye todavía:

- Consulta de incidencias.
- Actualización o cierre de incidencias.
- Autenticación y autorización.
- Análisis mediante inteligencia artificial.
- Notificaciones.

## Modelo de datos

Una incidencia contiene:

| Campo | Tipo | Obligatorio | Restricciones |
|---|---|---:|---|
| `id` | Long | Generado | Identificador único |
| `title` | String | Sí | Entre 1 y 120 caracteres después de recortar espacios |
| `description` | String | Sí | Entre 1 y 2000 caracteres después de recortar espacios |
| `status` | Enum | Generado | Valor inicial `OPEN` |
| `createdAt` | Instant | Generado | Fecha y hora de creación |

Los valores permitidos para `status` son `OPEN`, `IN_PROGRESS` y `CLOSED`. En esta especificación solo se permite crear incidencias con estado `OPEN`. Los estados `IN_PROGRESS` y `CLOSED` se reservan para futuras especificaciones.

Los valores de `title` y `description` deben recortarse al principio y al final antes de validarse y persistirse.

## API

### Crear una incidencia

```http
POST /api/incidents
Content-Type: application/json
```

Petición válida:

```json
{
  "title": "La aplicación no responde",
  "description": "Los usuarios reciben errores 500 desde las 10:00"
}
```

Respuesta correcta:

```http
HTTP/1.1 201 Created
Content-Type: application/json
```

```json
{
  "id": 1,
  "title": "La aplicación no responde",
  "description": "Los usuarios reciben errores 500 desde las 10:00",
  "status": "OPEN",
  "createdAt": "2026-08-20T10:00:00Z"
}
```

El servidor debe incluir una cabecera `Location` con la URL relativa de la incidencia creada. Para una incidencia con id `15`, el valor debe ser `/api/incidents/15`.

El campo `createdAt` debe almacenarse y devolverse en UTC usando formato ISO-8601.

## Validación y errores

La API debe devolver `400 Bad Request` cuando:

- Falte `title`.
- `title` sea nulo, vacío o contenga únicamente espacios.
- `title` supere los 120 caracteres.
- Falte `description`.
- `description` sea nula, vacía o contenga únicamente espacios.
- `description` supere los 2000 caracteres.
- El cuerpo no sea JSON válido.

La API debe devolver `415 Unsupported Media Type` cuando el `Content-Type` no sea `application/json`.

La API debe devolver `400 Bad Request` cuando falte el `Content-Type` o el cuerpo de la petición sea nulo.

Formato mínimo de error:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "La petición contiene errores de validación",
  "timestamp": "2026-08-20T10:00:00Z"
}
```

No es necesario exponer detalles internos de la base de datos.

## Criterios de aceptación

### Caso 1: creación válida

```text
Dado un título y una descripción válidos
Cuando se envía POST /api/incidents
Entonces la respuesta es 201 Created
Y la respuesta contiene un id
Y el estado es OPEN
Y la respuesta contiene createdAt
Y la incidencia queda persistida
```

### Caso 2: título vacío

```text
Dado un título vacío
Cuando se envía POST /api/incidents
Entonces la respuesta es 400 Bad Request
Y no se crea ninguna incidencia
```

### Caso 3: descripción ausente

```text
Dada una petición sin description
Cuando se envía POST /api/incidents
Entonces la respuesta es 400 Bad Request
Y no se crea ninguna incidencia
```

### Caso 4: título demasiado largo

```text
Dado un título de 121 caracteres
Cuando se envía POST /api/incidents
Entonces la respuesta es 400 Bad Request
Y no se crea ninguna incidencia
```

### Caso 5: descripción vacía

```text
Dada una descripción vacía o formada únicamente por espacios
Cuando se envía POST /api/incidents
Entonces la respuesta es 400 Bad Request
Y no se crea ninguna incidencia
```

### Caso 6: descripción demasiado larga

```text
Dada una descripción de 2001 caracteres después de recortar espacios
Cuando se envía POST /api/incidents
Entonces la respuesta es 400 Bad Request
Y no se crea ninguna incidencia
```

### Caso 7: JSON mal formado

```text
Dado un cuerpo que no es JSON válido
Cuando se envía POST /api/incidents con Content-Type application/json
Entonces la respuesta es 400 Bad Request
Y no se crea ninguna incidencia
```

### Caso 8: Content-Type no soportado

```text
Dada una petición con un Content-Type distinto de application/json
Cuando se envía POST /api/incidents
Entonces la respuesta es 415 Unsupported Media Type
Y no se crea ninguna incidencia
```

### Caso 9: cabecera Location y fecha UTC

```text
Dada una petición válida
Cuando se crea la incidencia
Entonces la respuesta contiene la cabecera Location
Y su valor es /api/incidents/{id}
Y la respuesta contiene createdAt en formato ISO-8601 UTC
```

### Caso 10: persistencia

```text
Dada una petición válida
Cuando la API responde 201 Created
Entonces existe una incidencia persistida con el mismo id
Y conserva el title y description recortados
Y su status es OPEN
Y su createdAt está expresado en UTC
```

## Diseño técnico esperado

La implementación debe separar:

- `IncidentController` para HTTP.
- `IncidentService` para el caso de uso.
- `IncidentRepository` para persistencia.
- Entidad JPA `Incident`.
- DTOs de petición y respuesta.
- Manejo centralizado de errores de validación.
- Uso de `jakarta.validation.*` y `jakarta.persistence.*`.

Para las pruebas se recomienda:

- `@WebMvcTest` para el controlador.
- `@DataJpaTest` para la persistencia.
- Un test de integración para verificar el flujo completo.

Las respuestas de error pueden implementarse mediante `ProblemDetail` o un DTO propio, siempre que respeten el contrato definido en esta especificación.

Los controladores no deben acceder directamente al repositorio.

## Verificación

Antes de marcar esta especificación como completada, ejecutar:

```powershell
mvn clean test
```

También debe existir al menos un test de integración del endpoint y tests para las validaciones principales.
