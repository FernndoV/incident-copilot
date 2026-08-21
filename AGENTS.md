# Incident Copilot - Project Instructions

## Objetivo

Construir una API REST para registrar, consultar y analizar incidencias de software.
El desarrollo debe seguir las especificaciones almacenadas en `docs/specs/`.

## Stack técnico

- Java 17
- Spring Boot 4.0.7
- Maven
- Spring MVC
- Spring Data JPA
- Bean Validation
- H2 para desarrollo y pruebas locales
- JUnit y las dependencias de testing de Spring Boot

## Arquitectura

La feature 001 conserva su organización por capas para mantener el alcance
controlado. Las nuevas features, empezando por la 002, deben usar arquitectura
hexagonal:

- `domain/model`: modelos y reglas sin dependencias de Spring, JPA ni MVC.
- `domain/port/in`: casos de uso expuestos al exterior.
- `domain/port/out`: puertos requeridos por la aplicación.
- `application`: orquestación y casos de uso.
- `adapter/in/web`: controladores, DTOs y traducción HTTP.
- `adapter/out`: persistencia e implementaciones de agentes.

El núcleo depende de puertos; los adaptadores dependen del núcleo. No exponer
entidades JPA desde controladores ni acoplar el dominio a infraestructura.
## Reglas de desarrollo

- La especificación funcional es la fuente de verdad.
- No implementar funcionalidades fuera del alcance de la especificación activa.
- Escribir o actualizar tests antes de implementar el comportamiento.
- No exponer entidades JPA directamente desde los controladores.
- Validar los cuerpos de las peticiones con Bean Validation.
- Devolver códigos HTTP coherentes y errores consistentes.
- Mantener los controladores delgados.
- No añadir dependencias sin justificarlo.
- No incluir secretos, claves API ni archivos `.env` en el repositorio.
- Preferir cambios pequeños y fáciles de revisar.

## Verificación obligatoria

Antes de considerar terminada una tarea:

```powershell
mvn clean test
```

Además, revisar:

```powershell
git diff
git status
```

## Comunicación de cambios

Al finalizar una tarea, informar de:

1. Archivos modificados.
2. Especificación implementada.
3. Tests ejecutados y resultado.
4. Riesgos o decisiones pendientes.

