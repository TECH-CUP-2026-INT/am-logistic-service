# Arquitectura

## Capas

```
controller/    -> REST controllers (capa de entrada)
dto/           -> DTOs de request/response, separados de las entidades
service/       -> Interfaces de negocio
service/impl/  -> Implementación de la lógica de negocio y validaciones
repository/    -> Spring Data JPA
entity/        -> Entidades JPA
enums/         -> Enumeraciones del dominio
mapper/        -> Conversión entity <-> DTO
adapter/       -> Puertos + adaptadores hacia otros microservicios
security/      -> JWT del Gateway (JwtClaimsFilter), API key interna y rol organizador
exception/     -> Excepciones de negocio + manejo global de errores
config/        -> Configuración de Spring (seguridad, interceptores, @Async)
```

## Modelo de datos

| Entidad | Descripción | Restricción clave |
|---|---|---|
| `DefinicionRefrigerio` | Qué refrigerio(s) corresponden a un equipo en un partido | única por `(partidoId, equipoId)` |
| `EntregaRefrigerio` | Entrega real de un refrigerio a un equipo o jugador | única por `(partidoId, tipoDestinatario, destinatarioId)` |
| `ItemDotacion` | Ítem de dotación (peto/balón/kit) con estado y responsable | no se puede marcar `ENTREGADO` dos veces; solo se puede registrar la devolución (`DEVUELTO`) si el estado actual es `ENTREGADO` |

**Supuesto de diseño:** todos los identificadores propios y externos
(`partidoId`, `equipoId`, `jugadorId`, `responsableId`, etc.) se modelan como
`UUID`. Si Torneos o Equipos exponen IDs numéricos, es un cambio mecánico de
tipo en entidades/DTOs/adaptadores.

## Seguridad

Mismo modelo de confianza que `am-matches-service` y
`am-notification-service`: el API Gateway valida la firma y expiración del
JWT antes de reenviar la petición; este servicio **no vuelve a verificar la
firma**.

- `security/JwtClaimsFilter.java` decodifica el `Authorization: Bearer <jwt>`
  (claims `sub` y el claim de roles, configurable vía
  `techcup.security.role-claim`) y puebla el `SecurityContext` con un
  `AuthenticatedUser(UUID userId, Set<String> roles)` y autoridades
  `ROLE_<ROL>`.
- `security/InternalApiKeyFilter.java` autentica llamadas servicio-a-servicio
  mediante el header `X-Internal-Api-Key`, con el mismo valor compartido
  (`techcup.security.internal.api-key`) que matches-service y
  notification-service.
- `config/SecurityConfig.java` exige autenticación en todos los endpoints
  salvo `/actuator/health` y Swagger.
- `security/OrganizadorInterceptor.java` responde `403` en los métodos
  anotados `@RequireOrganizador` si el usuario autenticado no tiene la
  autoridad `ROLE_ORGANIZADOR`.
- `security/CurrentUserProvider.java` obtiene el id del usuario autenticado
  desde el `SecurityContext` (ya no desde un header sin validar).
- `GET /api/auditoria/eventos` (RF-04) usa `@PreAuthorize("hasAnyRole('ADMIN','ORGANIZADOR')")`
  en lugar de `@RequireOrganizador`, porque a diferencia de los endpoints de
  escritura de dotación/refrigerios, este es de solo lectura y debe aceptar
  dos roles distintos; Spring Method Security ya está habilitado
  (`@EnableMethodSecurity` en `SecurityConfig`) y reutiliza las mismas
  autoridades `ROLE_<ROL>` que puebla `JwtClaimsFilter`, sin cambios en el
  filtro.

**Implicación operativa (no negociable), igual que en los otros dos
servicios propios:** como este servicio confía ciegamente en que el JWT ya
fue validado, **nunca debe exponerse directo a internet ni a otros servicios
que no sea el API Gateway**. Debe protegerse a nivel de red
(firewall/security group/service mesh).

## Integraciones con otros microservicios

No se tiene el código de Torneos, Equipos ni Auditoría (equipos dueños
externos a astromerge), así que cada integración se definió como un
**puerto** (interfaz) con su **adaptador** REST concreto:

| Puerto | Paquete | Uso | Estilo |
|---|---|---|---|
| `TorneoClientPort` | `adapter.torneos` | Validar que un partido/jornada existe | REST síncrono (bloqueante) |
| `EquipoClientPort` | `adapter.equipos` | Validar equipo/jugador destinatario | REST síncrono (bloqueante) |
| `AuditoriaClientPort` | `adapter.auditoria` | Reportar cada entrega registrada | REST asíncrono (`@Async`, fire-and-forget) |

**Por qué esta combinación:** Torneos y Equipos resuelven datos maestros que
Logística necesita *antes* de aceptar una operación — encajan naturalmente
en request/response síncrono. Auditoría es trazabilidad de "mejor
esfuerzo": un fallo al reportar no debe bloquear ni revertir una entrega ya
registrada en Logística, así que se implementó como una llamada REST no
bloqueante. Cada adaptador tiene un comentario `TODO` señalando qué ruta,
payload y semántica de error se **asumieron** y deben confirmarse con el
equipo dueño del servicio externo.

### Por qué REST y no un broker de mensajería

Igual que en `am-matches-service`: se evaluó mensajería asíncrona
(Kafka/RabbitMQ) para las integraciones entre los microservicios de la
plataforma, pero como todavía no hay un broker desplegado, se optó por
interfaces de dominio (puertos) con una única implementación REST
configurable por URL. Esto no bloquea el desarrollo esperando
infraestructura, y permite reemplazar la implementación por un publisher de
eventos sin tocar la capa de servicio — ver el `TODO` en
`AuditoriaClientAdapter` sobre migrar a un mecanismo de eventos/cola cuando
Auditoría lo defina.

### Servicios propios de astromerge (D3) y sus puertos

Para poder levantar los tres servicios del equipo a la vez en desarrollo
local sin choques de puerto:

| Servicio | Puerto app (Docker) | Puerto Postgres (host) |
|---|---|---|
| `am-matches-service` | `8080` | `5432` |
| `am-notification-service` | `8083` | `5433` |
| `am-logistic-service` | `8085` | `5434` |

`am-logistic-service` no llama hoy a `am-matches-service` ni a
`am-notification-service` directamente — sus tres adaptadores apuntan a
Torneos, Equipos y Auditoría (fuera de este equipo). Ver
[Anexos](anexos.md) para el detalle de qué integraciones están confirmadas
entre los tres servicios propios.

### Verificación de conectividad

Se levantó `am-logistic-service` junto con `am-matches-service` y
`am-notification-service` (`docker compose up --build` en los 3 repos a la
vez) y se confirmó: arranque limpio sin choque de puertos/Postgres,
`/actuator/health` en `200`, `403` sin JWT en un endpoint de escritura,
`201`→flujo feliz hasta el punto en que depende de Torneos (que no existe
en este workspace, falla de forma segura con `502` como está documentado),
y `200` con lista vacía en los endpoints de lectura. La seguridad JWT +
rol organizador introducida en esta auditoría quedó verificada en caliente,
no solo con tests unitarios.
