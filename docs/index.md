# Servicio de Logística (service-logistics)

Microservicio Spring Boot responsable de la operación **no deportiva** del
torneo universitario **TechCup Fútbol**: refrigerios y dotación (petos,
balones, kits) para equipos y jugadores. Es uno de ~12 microservicios
independientes de la plataforma Astro Merge; su único actor es el
**Organizador**.

[Ver en GitHub](https://github.com/TECH-CUP-2026-INT/am-logistic-service){ .md-button .md-button--primary }
[Explorar la API](api.md){ .md-button }

## Mapa de la documentación

| Sección | Contenido |
|---|---|
| [Introducción](introduccion.md) | Contexto, propósito y alcance del servicio |
| [Requerimientos](requerimientos.md) | Requisitos funcionales, no funcionales y prerrequisitos técnicos |
| [Configuración](configuracion.md) | Variables de entorno, ejecución local y despliegue con Docker |
| [Arquitectura](arquitectura.md) | Capas, modelo de datos e integraciones con otros servicios |
| [API](api.md) | Endpoints REST, autenticación y Swagger UI |
| [Pruebas](pruebas.md) | Estrategia de pruebas y cómo ejecutarlas |
| [Equipo](equipo.md) | Integrantes y roles del equipo TECH-CUP 2026 INT |
| [Anexos](anexos.md) | Glosario, hallazgos de seguridad y referencias |

## Resumen rápido

| Capa | Tecnología |
|---|---|
| Lenguaje / runtime | Java 21 |
| Framework | Spring Boot 3.5.6 |
| Build | Maven |
| Persistencia | PostgreSQL + Spring Data JPA |
| Migraciones | Flyway |
| API | Spring Web (REST) + springdoc-openapi |
| Seguridad | Spring Security (JWT del Gateway + API key interna) |
| CI/CD | GitHub Actions (build, test, cobertura, análisis estático, empaquetado, Docker) |
| Documentación | MkDocs + Material for MkDocs |

## Inicio rápido

```bash
# Levanta Postgres y el servicio (Flyway crea el esquema automáticamente)
docker compose up --build
```

Con el servicio corriendo, explora la API en
[http://localhost:8085/swagger-ui.html](http://localhost:8085/swagger-ui.html)
(puerto `8085` en `docker-compose.yml`, para convivir con matches-service y
notification-service en la misma máquina; `8080` por defecto fuera de Docker).

Para más detalle, ver [Configuración](configuracion.md) y [API](api.md).
