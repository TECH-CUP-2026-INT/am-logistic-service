# API

## Documentación interactiva (Swagger UI)

Con el servicio corriendo vía Docker Compose:
[http://localhost:8085/swagger-ui.html](http://localhost:8085/swagger-ui.html)
(o `http://localhost:8080/swagger-ui.html` si se ejecuta con `mvnw
spring-boot:run` fuera de Docker).

La especificación OpenAPI cruda está disponible en `/v3/api-docs`.

## Autenticación

El API Gateway valida la firma y expiración del JWT antes de reenviar la
petición. Este servicio **no vuelve a verificar la firma**: decodifica los
claims del token (`JwtClaimsFilter`) para poblar el contexto de seguridad.

Para probar los endpoints protegidos desde Swagger UI, usa el botón
**Authorize** con cualquier JWT bien formado que incluya los claims `sub`
(UUID) y `roles` (debe incluir `organizador` para los endpoints de
escritura) — no hace falta que la firma sea válida, porque este servicio no
la verifica (esa es responsabilidad del Gateway en producción).

## Endpoints

| Método | Ruta | Rol requerido | Descripción |
|---|---|---|---|
| `POST` | `/api/refrigerios/definiciones` | organizador | Define refrigerio(s) para un equipo/partido |
| `GET` | `/api/refrigerios/definiciones?partidoId=` | autenticado | Lista definiciones de un partido |
| `POST` | `/api/refrigerios/entregas` | organizador | Registra la entrega real de un refrigerio |
| `GET` | `/api/refrigerios/entregas?partidoId=` | autenticado | Lista entregas de un partido |
| `POST` | `/api/dotacion` | organizador | Registra un ítem de dotación como `PENDIENTE` |
| `PATCH` | `/api/dotacion/{itemId}/entrega` | organizador | Marca un ítem de dotación como `ENTREGADO` |
| `PATCH` | `/api/dotacion/{itemId}/devolucion` | organizador | Registra la devolución de un ítem de dotación (`ENTREGADO` → `DEVUELTO`) |
| `GET` | `/api/dotacion?equipoId=&estado=` | autenticado | Lista ítems de dotación de un equipo |
| `GET` | `/api/auditoria/eventos` | admin u organizador | Lista el feed local de eventos ya ocurridos en Logística (RF-04) |

## Ejemplo: definir un refrigerio

`POST /api/refrigerios/definiciones`

Request (`CrearDefinicionRefrigerioRequest`):

```json
{
  "partidoId": "9a8b7c6d-5e4f-3a2b-1c0d-9e8f7a6b5c4d",
  "equipoId": "b2f1e2b0-7c1a-4e9a-9b1a-2f0e1c8d5a11",
  "items": [
    { "descripcion": "Sandwich", "cantidad": 12 },
    { "descripcion": "Botella de agua", "cantidad": 12 }
  ]
}
```

Response `201 Created` (`DefinicionRefrigerioResponse`):

```json
{
  "id": "1f2e3d4c-5b6a-4978-8a9b-0c1d2e3f4a5b",
  "partidoId": "9a8b7c6d-5e4f-3a2b-1c0d-9e8f7a6b5c4d",
  "equipoId": "b2f1e2b0-7c1a-4e9a-9b1a-2f0e1c8d5a11",
  "items": [
    { "descripcion": "Sandwich", "cantidad": 12 },
    { "descripcion": "Botella de agua", "cantidad": 12 }
  ],
  "observaciones": null,
  "creadoPorId": "a1c3d4e5-1234-4a5b-8c9d-0e1f2a3b4c5d",
  "fechaCreacion": "2026-07-12T20:12:45Z"
}
```

Un segundo `POST` con el mismo `(partidoId, equipoId)` responde `409
Conflict` (`DuplicateResourceException`).

## Ejemplo: devolver un ítem de dotación

`PATCH /api/dotacion/{itemId}/devolucion`

Requiere rol organizador. Solo es válido si el ítem está actualmente en
estado `ENTREGADO`; el `recibidoPorId` no viaja en el body, es el
organizador autenticado (JWT) quien registra la devolución hecha por el
árbitro.

Request (`RegistrarDevolucionDotacionRequest`, body opcional):

```json
{ "observaciones": "Peto devuelto en buen estado" }
```

Response `200 OK` (`ItemDotacionResponse`):

```json
{
  "id": "3c1d2e3f-4a5b-4978-8a9b-0c1d2e3f4a5b",
  "equipoId": "b2f1e2b0-7c1a-4e9a-9b1a-2f0e1c8d5a11",
  "tipoItem": "PETO",
  "cantidad": 1,
  "estado": "DEVUELTO",
  "responsableAsignadoId": "6a5b4c3d-2e1f-4a5b-8c9d-0e1f2a3b4c5d",
  "entregadoPorId": "6a5b4c3d-2e1f-4a5b-8c9d-0e1f2a3b4c5d",
  "fechaRegistro": "2026-07-10T08:00:00Z",
  "fechaEntrega": "2026-07-10T09:00:00Z",
  "recibidoPorId": "a1c3d4e5-1234-4a5b-8c9d-0e1f2a3b4c5d",
  "fechaDevolucion": "2026-07-12T20:12:45Z",
  "observaciones": "Peto devuelto en buen estado"
}
```

Devolver un ítem que no está `ENTREGADO` (por ejemplo, aún `PENDIENTE` o ya
`DEVUELTO`) responde `409 Conflict` (`DuplicateResourceException`).

## Ejemplo: consultar el feed de auditoría (RF-04)

`GET /api/auditoria/eventos`

Requiere rol `ADMIN` u `ORGANIZADOR` (`@PreAuthorize`, no
`@RequireOrganizador`: cualquiera de los dos roles pasa). Es de solo
lectura: no escribe ningún registro nuevo, solo agrega en memoria y ordena
descendentemente por fecha los eventos ya ocurridos sobre las 3 entidades
propias de Logística.

Response `200 OK` (`List<AuditEventResponse>`):

```json
[
  {
    "tipo": "DOTACION_DEVUELTA",
    "entidadId": "3c1d2e3f-4a5b-4978-8a9b-0c1d2e3f4a5b",
    "actorId": "a1c3d4e5-1234-4a5b-8c9d-0e1f2a3b4c5d",
    "timestamp": "2026-07-12T20:12:45Z",
    "detalle": "Devolucion de PETO para equipo b2f1e2b0-7c1a-4e9a-9b1a-2f0e1c8d5a11"
  },
  {
    "tipo": "DEFINICION_REFRIGERIO_CREADA",
    "entidadId": "1f2e3d4c-5b6a-4978-8a9b-0c1d2e3f4a5b",
    "actorId": "a1c3d4e5-1234-4a5b-8c9d-0e1f2a3b4c5d",
    "timestamp": "2026-07-12T20:12:45Z",
    "detalle": "Definicion de refrigerio para equipo b2f1e2b0-7c1a-4e9a-9b1a-2f0e1c8d5a11 en partido 9a8b7c6d-5e4f-3a2b-1c0d-9e8f7a6b5c4d"
  }
]
```

Un usuario autenticado sin rol `ADMIN` ni `ORGANIZADOR` (ej. `JUGADOR`)
recibe `403 Forbidden`.

## Errores

Los errores de negocio (duplicado, recurso no encontrado, rol insuficiente,
falla al validar contra Torneos/Equipos, etc.) se manejan de forma
centralizada en `GlobalExceptionHandler` (`@RestControllerAdvice`) y se
devuelven con el DTO `ErrorResponse`, con un código HTTP acorde:

| Código | Causa |
|---|---|
| `400` | Validación de payload (Bean Validation) |
| `401` | Sin autenticación (falta o es inválido el JWT) |
| `403` | Autenticado pero sin rol organizador (`ForbiddenRoleException`) |
| `404` | Recurso no encontrado (`RecursoNoEncontradoException`) |
| `409` | Duplicado (`DuplicateResourceException`) |
| `502` | Un servicio externo (Torneos/Equipos/Auditoría) no respondió (`IntegrationException`) |
