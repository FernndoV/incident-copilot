# Especificación 002: Analizar una incidencia mediante agentes especializados

## Estado

Propuesta para revisión.

## Objetivo

Generar un análisis técnico de una incidencia utilizando un agente coordinador y
varios agentes especializados. El resultado debe ser estructurado, trazable y
verificable.

## Alcance de esta iteración

Esta primera iteración define el contrato y la arquitectura del flujo multiagente.
El análisis se ejecutará con implementaciones deterministas locales, sin llamadas
a modelos externos.

Incluye:

- Un agente coordinador.
- Agentes especializados de clasificación, análisis técnico y seguridad.
- Un resultado unificado.
- Identificación de los agentes que participaron.
- Tests con datos conocidos.
- Separación explícita entre agentes de la aplicación y subagentes de Codex.

No incluye todavía:

- OpenAI API.
- Persistencia del análisis.
- Ejecución de comandos sobre sistemas externos.
- Lectura de logs reales.
- Comunicación entre agentes mediante red.

## Flujo esperado

```text
Incident
   ↓
CoordinatorAgent
   ├── TriageAgent
   ├── TechnicalAnalysisAgent
   └── SecurityAgent
   ↓
IncidentAnalysis
```

El coordinador debe invocar a los agentes especializados, recoger sus resultados
y construir una respuesta final. Un fallo de un agente no debe ocultarse: debe
quedar registrado como advertencia en el resultado.

## Contrato de entrada

El endpoint no acepta cuerpo HTTP. El caso de uso recupera la incidencia actual
mediante un puerto de salida y construye un `IncidentContext` inmutable con:

| Campo | Tipo | Obligatorio | Descripción |
|---|---|---:|---|
| `incidentId` | Long | Sí | Identificador de la incidencia |
| `title` | String | Sí | Título validado de la incidencia |
| `description` | String | Sí | Descripción validada de la incidencia |
| `location` | Object | No | Ubicación registrada |
| `createdAt` | DateTime | Sí | Fecha de creación |

Los agentes reciben únicamente este contexto. Un id cero o negativo devuelve
`400 Bad Request`.
`r`n## Contrato de salida

El análisis debe contener:

| Campo | Tipo | Obligatorio | Descripción |
|---|---|---:|---|
| `incidentId` | Long | Sí | Identificador de la incidencia |
| `category` | Enum | Sí | Categoría principal |
| `severity` | Enum | Sí | Severidad estimada |
| `summary` | String | Sí | Resumen técnico |
| `rootCauseHypotheses` | Lista de String | Sí | Hipótesis posibles |
| `recommendedActions` | Lista de String | Sí | Acciones recomendadas |
| `warnings` | Lista de String | Sí | Advertencias o fallos parciales |
| `participatingAgents` | Lista de String | Sí | Agentes que respondieron correctamente |
| `agentContributions` | Lista de Object | Sí | Resultado individual de cada agente |
| `confidence` | Decimal | Sí | Valor entre 0 y 1 |

Las categorías permitidas son `APPLICATION`, `DATABASE`, `NETWORK`, `SECURITY`
y `UNKNOWN`.

Las severidades permitidas son `LOW`, `MEDIUM`, `HIGH` y `CRITICAL`.

Cada contribución debe incluir `agentId`, `status`, `confidence` y las
conclusiones que el agente haya producido. Los estados de una contribución son
`SUCCESS` y `FAILED`.

## API

```http
POST /api/incidents/{id}/analysis
```

Respuesta correcta:

```http
HTTP/1.1 200 OK
Content-Type: application/json
```

```json
{
  "incidentId": 1,
  "category": "DATABASE",
  "severity": "HIGH",
  "summary": "La incidencia parece relacionada con errores de acceso a base de datos.",
  "rootCauseHypotheses": [
    "Pool de conexiones agotado",
    "Base de datos no disponible"
  ],
  "recommendedActions": [
    "Revisar métricas del pool",
    "Comprobar conectividad con la base de datos"
  ],
  "warnings": [],
  "participatingAgents": [
    "triage-agent",
    "technical-analysis-agent",
    "security-agent"
  ],
  "agentContributions": [
    {
      "agentId": "triage-agent",
      "status": "SUCCESS",
      "confidence": 0.8,
      "summary": "Clasificación de base de datos",
      "hypotheses": [],
      "recommendations": [],
      "errorCode": null
    }
  ],`r`n  "confidence": 0.86
}
```

## Errores

- Incidencia inexistente: `404 Not Found`.
- Identificador no numérico: `400 Bad Request`.
- Fallo parcial de un agente: `200 OK` con una entrada en `warnings`.
- Fallo de todos los agentes: `500 Internal Server Error`.
- Fallo del coordinador: `500 Internal Server Error` sin detalles internos.

Para errores 400, 404 y 500, el cuerpo usa `ProblemDetail` con un `type`,
un `title` y un `code` estable. Nunca se exponen stack traces, nombres de
clases, mensajes de infraestructura ni datos sensibles. El fallo total usa el
código `ANALYSIS_UNAVAILABLE`; un fallo inesperado del coordinador usa
`ANALYSIS_COORDINATOR_ERROR`.
`r`n## Reglas de agregación

- El orden de severidad es `LOW < MEDIUM < HIGH < CRITICAL`.
- En empates de categoría se prefiere `triage-agent`; si no participa, se
  conserva el agente con mayor confianza y, después, el orden fijo de registro.
- Las contribuciones se ordenan siempre según la lista inyectada al coordinador.
- Hipótesis y recomendaciones conservan el orden de los agentes y de cada lista,
  eliminando duplicados tras recortar espacios y comparar sin distinguir
  mayúsculas/minúsculas.
- El resumen se genera de forma determinista a partir de categoría y severidad.
- Las confianzas fuera de [0,1] se consideran inválidas y no participan en la
  media; si no hay valores válidos, la media es 0.
- En esta iteración los agentes se ejecutan secuencialmente y no tienen efectos
  secundarios. No se define timeout; los límites operativos quedan fuera de
  alcance hasta la integración con IA externa.
- La severidad final es la más alta propuesta por los agentes que respondieron.
- La categoría final es la propuesta por `triage-agent`; si no responde, se usa
  la propuesta disponible con mayor confianza; si ninguna existe, `UNKNOWN`.
- Las recomendaciones se combinan eliminando duplicados y conservando un orden
  estable.
- `confidence` es la media de las confianzas válidas de los agentes que
  respondieron.
- Si al menos un agente responde correctamente, el coordinador devuelve `200 OK`
  y registra los fallos en `warnings`.
- Si todos fallan, devuelve `500 Internal Server Error`.


## Responsabilidades independientes

- `TriageAgent`: determina categoría y severidad iniciales.
- `TechnicalAnalysisAgent`: produce hipótesis de causa raíz y acciones técnicas.
- `SecurityAgent`: identifica riesgos de seguridad y acciones de protección.
- `CoordinatorAgent`: coordina, captura fallos, agrega resultados y mantiene separadas las responsabilidades.

## Criterios de aceptación

### Caso 1: análisis completo

```text
Dada una incidencia existente
Cuando se solicita POST /api/incidents/{id}/analysis
Entonces la respuesta es 200 OK
Y contiene category y severity
Y confidence está entre 0 y 1
Y participatingAgents no está vacío
Y warnings está vacío cuando todos los agentes responden
```

### Caso 2: incidencia inexistente

```text
Dado un id que no existe
Cuando se solicita el análisis
Entonces la respuesta es 404 Not Found
```

### Caso 3: identificador inválido

Dado un id no numérico o no positivo
Cuando se solicita el análisis
Entonces la respuesta es 400 Bad Request

### Caso 4: fallo parcial

```text
Dada una incidencia existente
Y uno de los agentes devuelve un error
Cuando se solicita el análisis
Entonces la respuesta es 200 OK
Y warnings contiene información del agente fallido
Y agentContributions contiene una contribución FAILED para ese agente
Y participatingAgents solo contiene agentes SUCCESS
```

### Caso 5: fallo total

```text
Dada una incidencia existente
Y todos los agentes fallan
Cuando se solicita el análisis
Entonces la respuesta es 500 Internal Server Error
Y no expone detalles internos
```

### Caso 6: resultado determinista y agregación

```text
Dada la misma incidencia
Cuando se solicita el análisis varias veces
Entonces la respuesta es equivalente en category, severity y recomendaciones
```

## Diseño técnico esperado

Definir una interfaz común para agentes especializados y evitar que el
`IncidentController` conozca sus implementaciones concretas.

El coordinador debe depender de abstracciones y poder recibir una lista de
agentes. Esto permitirá sustituir los agentes deterministas por agentes basados
en IA en una futura iteración.

Cada agente debe devolver un resultado estructurado o un error controlado.

La entrada del coordinador debe ser un `IncidentContext` inmutable. La salida debe conservar `agentContributions` para trazabilidad.

La arquitectura debe separar adaptadores HTTP y persistencia del núcleo de agentes y coordinador. El dominio no depende de Spring MVC, JPA ni infraestructura; los adaptadores implementan puertos; y los agentes deterministas son reemplazables por adaptadores basados en IA.

La implementación debe usar inyección por constructor, validación Jakarta y `ProblemDetail`, siendo compatible con Spring Boot 4 y Java 17.
No se deben ejecutar comandos externos ni enviar datos fuera del proceso.

La arquitectura debe separar adaptadores HTTP y persistencia del núcleo de
agentes y del coordinador, siguiendo principios de arquitectura hexagonal.

Puertos mínimos:

- `AnalyzeIncidentUseCase`: puerto de entrada para solicitar el análisis.
- `IncidentQueryPort`: puerto de salida para recuperar una incidencia como modelo
  de dominio, sin exponer JPA.
- `SpecializedAgent`: puerto de salida común para agentes especializados, con
  una operación que recibe `IncidentContext` y devuelve una contribución.
- El adaptador HTTP solo traduce la petición y la respuesta.
- El adaptador de persistencia traduce entre entidad JPA y modelo de dominio.

Estructura orientativa:

    analysis/domain/model
    analysis/domain/port/in
    analysis/domain/port/out
    analysis/application
    analysis/adapter/in/web
    analysis/adapter/out
## Verificación

Antes de marcar esta especificación como completada:

```powershell
mvn test
```

Debe existir al menos un test del coordinador, un test de fallo parcial y un test
del endpoint HTTP.



