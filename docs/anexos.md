# Anexos

## Glosario

| Término | Significado |
|---|---|
| Organizador | Actor principal de este servicio; define refrigerios y registra entregas |
| Refrigerio | Alimento/bebida definido para un equipo en un partido |
| Dotación | Ítem físico (peto, balón, kit) prestado a un árbitro durante el torneo |
| Puerto (arquitectura) | Interfaz de dominio que abstrae una integración externa |
| Best-effort | Llamada a otro servicio cuyo fallo se registra pero no bloquea el flujo principal |
| `ROLE_ORGANIZADOR` | Autoridad de Spring Security exigida en los endpoints de escritura |
| `X-Internal-Api-Key` | Header compartido entre los servicios propios de astromerge para autenticar llamadas servicio-a-servicio |

## Hallazgos de seguridad

Hallazgos corregidos durante la revisión de seguridad de este servicio:

- **Confianza ciega en headers sin validar (vulnerabilidad real)**: la
  versión anterior leía `X-User-Id`/`X-User-Role` directamente de la
  petición HTTP sin que nada los validara — cualquiera que pudiera alcanzar
  el servicio (no solo el Gateway) podía fabricar esos headers y obtener
  acceso de organizador. Corregido: se introdujo `JwtClaimsFilter` (idéntico
  en espíritu al de `am-matches-service` y `am-notification-service`), que
  decodifica el JWT ya validado por el Gateway, y `SecurityConfig` ahora
  exige autenticación en todos los endpoints salvo `/actuator/health` y
  Swagger. `OrganizadorInterceptor` verifica la autoridad `ROLE_ORGANIZADOR`
  del `SecurityContext`, no un header.
- **Sin autenticación servicio-a-servicio**: no existía forma de distinguir
  una llamada interna de la plataforma. Se añadió `InternalApiKeyFilter`
  con el header `X-Internal-Api-Key`, consistente con el mismo mecanismo ya
  usado por notification-service.
- **Warning de credencial en memoria generada por Spring Boot**: al añadir
  `spring-boot-starter-security` sin `UserDetailsService` propio, Spring
  Boot generaría y loguearía una contraseña de desarrollo en cada arranque.
  Se excluyó explícitamente `UserDetailsServiceAutoConfiguration` (misma
  solución aplicada en `am-matches-service`).

**Implicación operativa (no negociable):** como este servicio confía
ciegamente en que el JWT ya fue validado, **nunca debe exponerse directo a
internet ni a otros servicios que no sea el API Gateway** — cualquiera que
le hable directamente puede fabricar un token con cualquier `sub`/`roles` y
pasar la autorización. Debe protegerse a nivel de red (firewall/security
group/service mesh) para que solo el Gateway pueda alcanzar su puerto.

Puntos que quedan fuera del alcance de este servicio, por diseño
(responsabilidad de la infraestructura/Gateway): HTTPS/TLS, rate limiting, y
CORS.

## Pipeline de CI/CD

Definido en
[`.github/workflows/ci-push.yml`](https://github.com/TECH-CUP-2026-INT/am-logistic-service/blob/main/.github/workflows/ci-push.yml)
(más `pr-master.yml` y `pr-qa.yml` para pull requests), calcado del pipeline
ya probado de `am-matches-service`. Etapas:

1. **Checkout** del código (`actions/checkout`).
2. **Configuración del entorno**: JDK 21 (Temurin) con cache de dependencias
   Maven.
3. **Compilación** (`./mvnw compile`).
4. **Ejecución de pruebas** (`./mvnw test`), con publicación del reporte de
   Surefire como artefacto.
5. **Gate de cobertura** (`./mvnw jacoco:check`, mínimo 80% de línea),
   con publicación del reporte de JaCoCo como artefacto.
6. **Análisis estático** con SonarQube (`./mvnw sonar:sonar`), incluyendo el
   reporte de cobertura JaCoCo, omitido en pull requests desde forks.
7. **Empaquetado** del JAR (`./mvnw package -DskipTests`) y publicación como
   artefacto de GitHub Actions.
8. **Dockerización**: build de la imagen con el `Dockerfile` multi-stage del
   repositorio, solo en eventos `push` a `master`/`qa`.
9. **Despliegue**: a Azure App Service, solo en `push` a `master`.

## Referencias

- [MkDocs](https://www.mkdocs.org/) — generador de sitios de documentación
  estática usado en este proyecto.
- [Material for MkDocs](https://squidfunk.github.io/mkdocs-material/) — tema
  usado para el sitio.
- [Spring Boot](https://spring.io/projects/spring-boot) — framework del
  servicio.
- [Flyway](https://flywaydb.org/) — herramienta de migraciones de esquema.
- [springdoc-openapi](https://springdoc.org/) — generación de la
  especificación OpenAPI y Swagger UI.
- [JaCoCo](https://www.jacoco.org/jacoco/) — cobertura de pruebas.

## Historial de cambios

| Fecha | Cambio |
|---|---|
| 2026-07-12 | Reemplazado el modelo de confianza en headers sin validar por JWT del Gateway + API key interna (`JwtClaimsFilter`, `InternalApiKeyFilter`, `SecurityConfig`). Añadido gate de cobertura JaCoCo (80%), pipeline de CI/CD (calcado de matches-service) y documentación técnica en MkDocs. Corregida la colisión de puertos con `am-matches-service` en `docker-compose.yml`. |
