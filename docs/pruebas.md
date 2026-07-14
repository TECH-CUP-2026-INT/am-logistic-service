# Pruebas

## Cómo ejecutar las pruebas

```bash
# Suite completa (usa H2 en memoria, no requiere Postgres real)
./mvnw test

# Suite completa + gate de cobertura (JaCoCo >= 80%)
./mvnw verify
```

`src/test/resources/application.properties` configura H2 en memoria para el
test de contexto de Spring (`ServiceLogisticsApplicationTests`), así que la
suite completa no depende de un Postgres corriendo.

## Qué cubren las pruebas

| Área | Cubre |
|---|---|
| `service/impl/*ServiceImplTest` | Lógica de negocio de los 3 requerimientos: definición de refrigerios, registro de entregas y control de dotación, incluyendo validaciones de duplicados y recurso no encontrado |
| `controller/*ControllerTest` | Contrato HTTP de cada endpoint: `201`/`200` en el camino feliz, `403` sin rol organizador, `400` en validación de payload |
| `security/*` | `JwtClaimsFilter` (extracción de `sub`/roles desde el JWT), `InternalApiKeyFilter` (API key servicio-a-servicio), `OrganizadorInterceptor` (rechazo sin rol organizador), `CurrentUserProvider` |
| `exception/*` | `GlobalExceptionHandler`, códigos de estado y forma del `ErrorResponse` para cada tipo de excepción de negocio |
| `adapter/*` | Adaptadores REST hacia Torneos/Equipos/Auditoría: camino feliz y manejo de fallos del servicio externo |
| `ServiceLogisticsApplicationTests` | Carga del contexto de Spring Boot (H2 en memoria) |

## Cobertura mínima

El pipeline de CI aplica un gate de cobertura de línea del **80%** con
JaCoCo (`jacoco-maven-plugin`, goal `check`, atado a la fase `verify`),
excluyendo DTOs, entidades JPA, clases de configuración y la clase principal
de arranque (paquetes sin lógica de negocio propia). Si la cobertura cae por
debajo del umbral, el build falla.

## Pruebas en el pipeline de CI

El workflow de GitHub Actions (`.github/workflows/ci-push.yml` y los
equivalentes `pr-master.yml`/`pr-qa.yml`) ejecuta `./mvnw test`, publica el
reporte de Surefire, corre `./mvnw jacoco:check` para el gate de cobertura,
publica el reporte de JaCoCo como artefacto, y solo si todo eso pasa corre
el análisis estático con SonarQube y empaqueta el JAR. Si alguna prueba
falla o la cobertura no llega al 80%, el pipeline se detiene y no se genera
ni el JAR ni la imagen Docker.

Ver [Configuración](configuracion.md) para variables de entorno y
[Arquitectura](arquitectura.md) para el detalle de las reglas de negocio que
estas pruebas verifican.
