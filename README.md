# Servicio de Logística — TechCup Fútbol

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
repository/    -> Spring Data JPA
entity/        -> Entidades JPA
enums/         -> Enumeraciones del dominio
mapper/        -> Conversión entity <-> DTO
adapter/       -> Puertos + adaptadores hacia otros microservicios (ver abajo)
security/      -> Verificación del rol organizador (headers del Gateway)
exception/     -> Excepciones de negocio + manejo global de errores
config/        -> Configuración de Spring (interceptores, @Async)
```

## Modelo de datos

| Entidad | Descripción | Restricción clave |
|---|---|---|
| `DefinicionRefrigerio` | Qué refrigerio(s) corresponden a un equipo en un partido | única por `(partidoId, equipoId)` |
| `EntregaRefrigerio` | Entrega real de un refrigerio a un equipo o jugador | única por `(partidoId, tipoDestinatario, destinatarioId)` |
| `ItemDotacion` | Ítem de dotación (peto/balón/kit) con estado y responsable | no se puede marcar `ENTREGADO` dos veces |

**Supuesto de diseño:** todos los identificadores propios y externos
(`partidoId`, `equipoId`, `jugadorId`, `responsableId`, etc.) se modelan como
`UUID`. Si Torneos o Equipos exponen IDs numéricos, es un cambio mecánico de
tipo en entidades/DTOs/adaptadores.

## Seguridad

El JWT ya es validado por el API Gateway. Este servicio **no** valida firmas;
solo espera que el Gateway reenvíe la identidad del usuario en headers:

| Header | Uso |
|---|---|
| `X-User-Id` | UUID del usuario autenticado (se usa como responsable/creador) |
| `X-User-Role` | Rol del usuario; los endpoints de escritura exigen `organizador` |

Los métodos de controller anotados con `@RequireOrganizador` son interceptados
por `OrganizadorInterceptor`, que responde `403` si el rol no coincide. Los
endpoints de solo lectura (`GET`) no exigen rol específico.

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

Levanta Postgres y el servicio con un solo comando; las migraciones de Flyway
se aplican automáticamente al arrancar:

```bash
docker compose up --build
```

El servicio queda disponible en `http://localhost:8080`. Por defecto,
`TORNEOS_SERVICE_URL`, `EQUIPOS_SERVICE_URL` y `AUDITORIA_SERVICE_URL` apuntan
a `host.docker.internal:8081/8082/8083` (ajusta `docker-compose.yml` cuando
esos servicios existan en la plataforma).

### Opción 2: Maven local

Requiere Java 21, Maven (o el wrapper incluido) y un PostgreSQL accesible.

```bash
# Variables de entorno esperadas (con valores por defecto para desarrollo local)
DB_URL=jdbc:postgresql://localhost:5432/service_logistics
DB_USERNAME=postgres
DB_PASSWORD=postgres
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
usa H2 en memoria (ver `src/test/resources/application.properties`) para no
depender de un Postgres real.

**Verificación end-to-end:** además de las pruebas unitarias, el servicio se
levantó completo con `docker compose` (Postgres real + migraciones Flyway) y
se probó con mocks HTTP de Torneos/Equipos/Auditoría, confirmando: arranque
limpio, aplicación de las 3 migraciones, `/actuator/health`, Swagger UI,
rechazo `403` sin rol organizador, error `400` de validación, error `502`
cuando un servicio externo no responde, el flujo feliz completo (crear
definición → registrar entrega → registrar dotación → marcar entregada), los
tres `409` de duplicado, y la llamada asíncrona real a Auditoría tras cada
entrega.

## Endpoints principales

| Método | Ruta | Rol requerido | Descripción |
|---|---|---|---|
| `POST` | `/api/refrigerios/definiciones` | organizador | Define refrigerio(s) para un equipo/partido |
| `GET` | `/api/refrigerios/definiciones?partidoId=` | — | Lista definiciones de un partido |
| `POST` | `/api/refrigerios/entregas` | organizador | Registra la entrega real de un refrigerio |
| `GET` | `/api/refrigerios/entregas?partidoId=` | — | Lista entregas de un partido |
| `POST` | `/api/dotacion` | organizador | Registra un ítem de dotación como `PENDIENTE` |
| `PATCH` | `/api/dotacion/{itemId}/entrega` | organizador | Marca un ítem de dotación como `ENTREGADO` |
| `GET` | `/api/dotacion?equipoId=&estado=` | — | Lista ítems de dotación de un equipo |

Para probar los endpoints protegidos desde Swagger UI, en "Try it out" completa
el header `X-User-Id` (cualquier UUID) y `X-User-Role: organizador`.

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
