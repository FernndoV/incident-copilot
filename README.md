# Incident Copilot

Incident Copilot es una API REST desarrollada con Java y Spring Boot para registrar incidencias de software y generar análisis técnicos mediante un flujo multiagente determinista.

El proyecto sirve como práctica de Spec-Driven Development, arquitectura hexagonal, testing y uso de agentes y subagentes durante el desarrollo.

## Funcionalidades

### Registro de incidencias

Permite crear incidencias con título y descripción validados. El estado inicial es `OPEN`.

```http
POST /api/incidents
```

```json
{
  "title": "Errores de conexión con la base de datos",
  "description": "La aplicación devuelve errores de timeout al acceder a la base de datos."
}
```

### Análisis multiagente

```http
POST /api/incidents/{id}/analysis
```

El flujo está compuesto por:

- `TriageAgent`: determina categoría y severidad inicial.
- `TechnicalAnalysisAgent`: genera hipótesis técnicas y acciones recomendadas.
- `SecurityAgent`: identifica riesgos y medidas de protección.
- `CoordinatorAgent`: coordina y agrega los resultados.

El resultado incluye categoría, severidad, resumen técnico, hipótesis, acciones,
confianza, agentes participantes, contribuciones individuales y advertencias.

Los agentes son deterministas y locales. No se realizan llamadas a modelos
externos ni se ejecutan comandos sobre sistemas externos.

## Gestión de errores

- `400 Bad Request`: identificador o petición incorrecta.
- `404 Not Found`: incidencia inexistente.
- `200 OK`: análisis con fallos parciales.
- `500 Internal Server Error`: fallo total o error del coordinador.

Los errores HTTP utilizan `ProblemDetail` y no exponen información interna.

## Arquitectura

La feature de análisis utiliza arquitectura hexagonal:

```text
adapter/in/web
        ↓
domain/port/in
        ↓
application
        ↓
domain/port/out
        ↑
adapter/out/agent
adapter/out/persistence
```

El dominio no depende de Spring MVC ni JPA. El coordinador depende de puertos,
por lo que los agentes deterministas pueden sustituirse posteriormente por
agentes basados en modelos de IA.

## Tecnologías

- Java 17.
- Spring Boot 4.0.7.
- Spring MVC.
- Spring Data JPA.
- H2.
- Jakarta Bean Validation.
- JUnit.
- Maven.

La aplicación se inicia por defecto en el puerto `8090`.

## Ejecución local

```bash
mvn spring-boot:run
```

API disponible en `http://localhost:8090`.

## Tests

```bash
mvn test
```

La suite incluye tests unitarios del coordinador y tests MVC del endpoint de
análisis, además de las pruebas de registro de incidencias.

## Metodología de desarrollo

El proyecto sigue Spec-Driven Development:

1. Se define la especificación en `docs/specs/`.
2. Se revisan requisitos, errores y criterios de aceptación.
3. Se implementan tests y código.
4. Se valida la arquitectura.
5. Se verifica la solución con Maven.
6. Se publica el trabajo mediante ramas y Pull Requests.

Durante el desarrollo se utilizan subagentes especializados para revisar la
calidad del código y el cumplimiento de la arquitectura hexagonal.

## Estado actual

La versión actual permite registrar incidencias y analizarlas mediante agentes
deterministas locales.

La integración con APIs externas de IA, la persistencia de análisis y la lectura
de logs reales quedan como evoluciones futuras.
