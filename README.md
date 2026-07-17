# Servicio de Logística — TechCup Fútbol

[![CI](https://github.com/TECH-CUP-2026-INT/am-logistic-service/actions/workflows/ci-push.yml/badge.svg)](https://github.com/TECH-CUP-2026-INT/am-logistic-service/actions/workflows/ci-push.yml)
[![Docs](https://img.shields.io/badge/docs-mkdocs-6a1b9a)](https://tech-cup-2026-int.github.io/am-logistic-service/)

> **Estado: completo y verificado.** Las 3 funcionalidades, seguridad, manejo
> de errores, pruebas y despliegue con Docker están implementados y probados
> end-to-end (incluyendo casos de error). Lo único pendiente es **externo** a
> este repo: que Torneos, Equipos y Auditoría confirmen sus contratos reales
> y expongan sus servicios — ver [Pendientes](#pendientes-antes-de-conectar-en-serio)
> al final de este documento.

Microservicio Spring Boot responsable de la operación **no deportiva** del torneo
universitario TechCup Fútbol: refrigerios y dotación (petos, balones, kits) para
equipos y jugadores. No maneja partidos, alineaciones ni resultados — esa
información vive en otros microservicios de la plataforma.

El actor principal es el **Organizador**, quien define qué se entrega y registra
las entregas reales.

## Funcionalidades

1. **Definición de refrigerios por equipo** — el organizador define qué
   refrigerio(s) corresponden a un equipo en un partido, evitando duplicar la
   definición para el mismo partido/equipo.
2. **Registro de entrega de refrigerios** — se registra la entrega real por
   equipo o por jugador, validando que no exista ya una entrega para el mismo
   destinatario en el mismo partido.
3. **Registro y control de dotación** (petos, balones, kits) — cada ítem tiene
   estado `PENDIENTE` o `ENTREGADO` y un responsable asociado en cada etapa.

**Regla transversal de trazabilidad:** toda entrega (refrigerio o dotación)
queda registrada con fecha, responsable y destinatario, y se reporta al
Servicio de Auditoría para su trazabilidad posterior.

## Arquitectura

```
controller/    -> REST controllers (capa de entrada)
dto/           -> DTOs de request/response, separados de las entidades
service/       -> Interfaces de negocio
service/impl/  -> Implementación de la lógica de negocio y validaciones
repository/    -> Spring Data MongoDB
entity/        -> Documentos MongoDB
enums/         -> Enumeraciones del dominio
mapper/        -> Conversión entity <-> DTO
adapter/       -> Puertos + adaptadores hacia otros microservicios (ver abajo)
security/      -> JWT del Gateway (JwtClaimsFilter), API key interna y rol organizador
exception/     -> Excepciones de negocio + manejo global de errores
config/        -> Configuración de Spring (interceptores, @Async)
```

## Modelo de datos

| Entidad | Descripción | Restricción clave |
|---|---|---|
| `DefinicionRefrigerio` | Qué refrigerio(s) corresponden a un equipo en un partido | única por `(partidoId, equipoId)` |
| `EntregaRefrigerio` | Entrega real de un refrigerio al Capitán del equipo clasificado | única por `(partidoId, capitanId)`; `capitanId` se valida contra Equipos como capitán del equipo |
| `ItemDotacion` | Unidad física individual de dotación (peto/balón/kit) entregada a un árbitro, con estado y responsable | un documento por unidad física (rastreo individual); solo puede entregarse a un árbitro; no se puede marcar `ENTREGADO` dos veces |

**Supuesto de diseño:** todos los identificadores propios y externos
(`partidoId`, `equipoId`, `jugadorId`, `responsableId`, etc.) se modelan como
`UUID`. Si Torneos o Equipos exponen IDs numéricos, es un cambio mecánico de
tipo en entidades/DTOs/adaptadores.

## Seguridad

El JWT ya es validado por el API Gateway; este servicio **no vuelve a
verificar la firma** (mismo modelo de confianza que `am-matches-service` y
`am-notification-service`). `JwtClaimsFilter` decodifica los claims del
`Authorization: Bearer <jwt>` (`sub` = id de usuario, claim de roles
configurable vía `techcup.security.role-claim`, por defecto `roles`) y puebla
el `SecurityContext` con un `AuthenticatedUser` y las autoridades
`ROLE_<ROL>`. `SecurityConfig` exige autenticación en todos los endpoints
salvo `/actuator/health` y Swagger.

Los métodos de controller anotados con `@RequireOrganizador` son
interceptados por `OrganizadorInterceptor`, que responde `403` si el usuario
autenticado no tiene la autoridad `ROLE_ORGANIZADOR` — ya no confía en un
header sin validar. La identidad del usuario para crear/registrar recursos se
obtiene con `CurrentUserProvider.getCurrentUserId()`, a partir del principal
autenticado (no de un header). Las llamadas servicio-a-servicio se autentican
con el header `X-Internal-Api-Key` (`InternalApiKeyFilter`), con el mismo
valor compartido (`techcup.security.internal.api-key` / env
`INTERNAL_API_KEY`) que matches-service y notification-service.

## Integraciones con otros microservicios

No se tiene el código de Torneos, Equipos ni Auditoría, así que cada
integración se definió como un **puerto** (interfaz) con su **adaptador** REST
concreto, dejando explícito qué contrato falta confirmar con el equipo dueño:

| Puerto | Paquete | Uso | Estilo |
|---|---|---|---|
| `TorneoClientPort` | `adapter.torneos` | Validar que un partido/jornada existe | REST síncrono (bloqueante) |
| `EquipoClientPort` | `adapter.equipos` | Validar equipo/jugador destinatario | REST síncrono (bloqueante) |
| `AuditoriaClientPort` | `adapter.auditoria` | Reportar cada entrega registrada | REST asíncrono (`@Async`, fire-and-forget) |

**Por qué esta combinación:** Torneos y Equipos resuelven datos maestros que
Logística necesita *antes* de aceptar una operación (¿existe este partido?,
¿este jugador pertenece a este equipo?) — encajan naturalmente en
request/response síncrono. Auditoría, en cambio, es trazabilidad de
"mejor esfuerzo": un fallo al reportar no debe bloquear ni revertir una
entrega ya registrada en Logística, así que se implementó como una llamada
REST no bloqueante. Cada adaptador tiene un comentario `TODO` señalando qué
ruta, payload y semántica de error se **asumieron** y deben confirmarse con
el equipo dueño del servicio. Cuando Auditoría defina un mecanismo de
eventos/cola, `AuditoriaClientAdapter` es el único punto a reemplazar.

## Cómo correr el servicio

### Opción 1: Docker Compose (recomendado)

Levanta MongoDB y el servicio con un solo comando:

```bash
docker compose up --build
```

El servicio queda disponible en `http://localhost:8080`. Por defecto,
`TORNEOS_SERVICE_URL`, `EQUIPOS_SERVICE_URL` y `AUDITORIA_SERVICE_URL` apuntan
a `host.docker.internal:8081/8082/8083` (ajusta `docker-compose.yml` cuando
esos servicios existan en la plataforma).

### Opción 2: Maven local

Requiere Java 21, Maven (o el wrapper incluido) y un MongoDB accesible (local
o Azure Cosmos DB for MongoDB vCore).

```bash
# Variables de entorno esperadas (con valores por defecto para desarrollo local)
MONGODB_URI=mongodb://localhost:27017/service_logistics
TORNEOS_SERVICE_URL=http://localhost:8081
EQUIPOS_SERVICE_URL=http://localhost:8082
AUDITORIA_SERVICE_URL=http://localhost:8083

./mvnw spring-boot:run
```

### Observabilidad y documentación de la API

| Recurso | Ruta | Uso |
|---|---|---|
| Health check | `GET /actuator/health` | Para que el API Gateway u otros servicios verifiquen que Logística está arriba |
| Swagger UI | `GET /swagger-ui.html` | Contrato interactivo de la API para los demás equipos |
| OpenAPI JSON | `GET /v3/api-docs` | Especificación machine-readable de la API |

## Pruebas

```bash
./mvnw test
```

Incluye pruebas unitarias (Mockito) de la lógica de negocio de los 3
requerimientos funcionales, cubriendo casos felices y validaciones de
duplicados/no-encontrado. El test de contexto de Spring (`ServiceLogisticsApplicationTests`)
levanta un contenedor MongoDB real vía Testcontainers (ver
`AbstractMongoIntegrationTest`), por lo que requiere Docker disponible en el
entorno donde se ejecuten los tests.

**Verificación end-to-end:** además de las pruebas unitarias, el servicio se
levantó completo con `docker compose` (MongoDB real) y
se probó con mocks HTTP de Torneos/Equipos/Auditoría, confirmando: arranque
limpio, creación de los índices compuestos en MongoDB, `/actuator/health`, Swagger UI,
rechazo `403` sin rol organizador, error `400` de validación, error `502`
cuando un servicio externo no responde, el flujo feliz completo (crear
definición → registrar entrega → registrar dotación → marcar entregada), los
tres `409` de duplicado, y la llamada asíncrona real a Auditoría tras cada
entrega.

## Endpoints principales

| Método | Ruta | Rol requerido | Descripción |
|---|---|---|---|
| `POST` | `/api/refrigerios/definiciones` | organizador | Define refrigerio(s) para un equipo/partido (solo equipos clasificados a segunda fase) |
| `GET` | `/api/refrigerios/definiciones?partidoId=` | admin, organizador | Lista definiciones de un partido |
| `POST` | `/api/refrigerios/entregas` | organizador | Registra la entrega real de un refrigerio al Capitán del equipo clasificado |
| `GET` | `/api/refrigerios/entregas?partidoId=` | admin, organizador | Lista entregas de un partido |
| `POST` | `/api/dotacion` | organizador | Registra items de dotación como `PENDIENTE`, uno por unidad física, a un árbitro |
| `PATCH` | `/api/dotacion/{itemId}/entrega` | organizador | Marca un ítem de dotación como `ENTREGADO` |
| `PATCH` | `/api/dotacion/{itemId}/devolucion` | organizador | Registra la devolución de un ítem de dotación |
| `GET` | `/api/dotacion?arbitroId=&estado=` | admin, organizador | Lista ítems de dotación de un árbitro |
| `GET` | `/api/auditoria/eventos` | admin, organizador | Lista eventos de auditoría (audit log) de Logística |

Para probar los endpoints protegidos desde Swagger UI, usa el botón
**Authorize** con cualquier JWT bien formado que incluya los claims `sub`
(UUID) y `roles` (debe incluir `organizador` para los endpoints de
escritura) — no hace falta que la firma sea válida, porque este servicio no
la verifica (esa es responsabilidad del Gateway en producción).

## Pendientes antes de conectar en serio

Todo lo que depende de este repo está terminado. Lo que falta es coordinación
con los otros equipos de la plataforma:

1. **Torneos** debe confirmar: la ruta real para consultar un partido, la
   forma del payload, y qué status code devuelve cuando no existe. Hoy se
   asume `GET /partidos/{id}` → `200 {id, jornadaId, fechaProgramada}` o `404`.
   Ver `TODO` en `adapter/torneos/TorneoClientAdapter.java`.
2. **Equipos** debe confirmar: la ruta para validar un equipo y para validar
   que un jugador pertenece a un equipo. Hoy se asume
   `GET /equipos/{id}` y `GET /equipos/{equipoId}/jugadores/{jugadorId}`,
   ambos `200`/`404`. Ver `TODO` en `adapter/equipos/EquipoClientAdapter.java`.
3. **Auditoría** debe confirmar: la ruta y payload para reportar una entrega
   (hoy se asume `POST /registros`), y si prefieren seguir recibiendo REST o
   migrar a un mecanismo de eventos/cola — el adaptador ya está aislado
   (`AuditoriaClientAdapter`) para que ese cambio no toque el resto del
   servicio. Ver `TODO` en `adapter/auditoria/AuditoriaClientAdapter.java`.
4. Una vez confirmados, solo hace falta: (a) ajustar la ruta/payload en el
   adaptador correspondiente si difiere de lo asumido, y (b) apuntar
   `TORNEOS_SERVICE_URL`, `EQUIPOS_SERVICE_URL` y `AUDITORIA_SERVICE_URL`
   (en `docker-compose.yml` o como variables de entorno) a las URLs reales
   del entorno donde se despliegue.
5. Confirmar si los IDs externos (`partidoId`, `equipoId`, `jugadorId`) son
   `UUID` (supuesto actual) o numéricos — si son numéricos, es un cambio de
   tipo mecánico en entidades, DTOs y adaptadores.

Mientras tanto, el servicio funciona de forma aislada y falla de forma segura
(`502 Bad Gateway`) si intenta validar contra un servicio externo que no
responde — no hay riesgo de que bloquee el resto de la plataforma.
