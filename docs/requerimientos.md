# Requerimientos

Requisitos funcionales tomados de la hoja de requerimientos del equipo
**astromerge** (dominio D3 — Operaciones y Comunicación) para el Servicio de
Logística, contrastados contra la implementación actual.

## Requisitos funcionales (hoja de requerimientos)

| ID | Requisito | Estado |
|---|---|---|
| RF-01 | Definición de refrigerios por equipo/partido a cargo del Organizador | ✅ Implementado (`POST /api/refrigerios/definiciones`) |
| RF-02 | Registro de entrega real de refrigerios (equipo o jugador), sin duplicados | ✅ Implementado (`POST /api/refrigerios/entregas`) |
| RF-03 | Registro de entrega de dotación a árbitros, con cada ítem rastreado individualmente | ✅ Implementado (`POST /api/dotacion`, `PATCH /api/dotacion/{itemId}/entrega`) |
| RF-04 | Consultar eventos del Servicio de Logística (audit log), accesible por Admin y Organizador | ✅ Implementado (`GET /api/auditoria/eventos`) — feed local de solo lectura que agrega, en memoria, los eventos ya ocurridos sobre las 3 entidades propias (definición creada, entrega registrada, dotación registrada/entregada/devuelta); no reemplaza el reporte best-effort al Servicio de Auditoría externo (`AuditoriaClientAdapter`), que sigue existiendo para trazabilidad entre servicios |
| RF-05 | Devolución de dotación: el Árbitro devuelve los ítems y el Organizador registra la devolución (estado `Devuelto`) | ✅ Implementado (`PATCH /api/dotacion/{itemId}/devolucion`) — nuevo estado `EstadoDotacion.DEVUELTO`, columnas `recibido_por_id`/`fecha_devolucion` (migración `V4__add_devolucion_dotacion.sql`); solo permitido si el ítem está en estado `ENTREGADO` |

!!! note "Huecos funcionales cerrados"
    RF-04 y RF-05 habían quedado identificados como huecos durante la
    auditoría previa de este servicio (pipeline, seguridad, cobertura de
    pruebas y documentación). Ambos se implementaron juntos en un mismo
    esfuerzo: RF-05 aportó el estado `DEVUELTO` que RF-04 necesitaba para
    exponer el evento `DOTACION_DEVUELTA` en el feed de auditoría, por lo
    que tenía sentido entregarlos en conjunto.

## Requisitos no funcionales

| ID | Requisito |
|---|---|
| RNF-01 | **Disponibilidad del flujo del Organizador**: un fallo al reportar a Auditoría no debe bloquear ni revertir el registro de una entrega ya hecha en Logística. |
| RNF-02 | **Seguridad de red**: el servicio no verifica la firma del JWT (responsabilidad del Gateway), por lo que debe permanecer inaccesible fuera de la red interna de la plataforma. |
| RNF-03 | **Integridad de datos**: no se permite duplicar una definición de refrigerio para el mismo `(partidoId, equipoId)`, ni una entrega para el mismo destinatario en el mismo partido, ni marcar un ítem de dotación como entregado dos veces, ni registrar su devolución si no está en estado `ENTREGADO`. |
| RNF-04 | **Resiliencia ante servicios externos no disponibles**: si Torneos, Equipos o Auditoría no responden, el servicio falla de forma segura (`502 Bad Gateway`) sin comprometer el resto de la plataforma. |
| RNF-05 | **Mantenibilidad**: la lógica de negocio debe estar desacoplada de los controllers REST y de los detalles de integración externa, detrás de interfaces (puertos/adaptadores). |
| RNF-06 | **Reproducibilidad del build**: el proyecto debe compilar, probar y empaquetarse de forma determinista vía Maven Wrapper, tanto en local como en CI. |
| RNF-07 | **Cobertura de pruebas**: al menos 80% de cobertura de línea sobre la lógica de negocio (excluyendo DTOs, entidades JPA y clases de configuración), verificado automáticamente en CI (JaCoCo). |
| RNF-08 | **Observabilidad operativa**: el servicio debe exponer endpoints de salud/métricas vía Spring Boot Actuator. |

## Prerrequisitos técnicos

Para desarrollar y ejecutar el servicio localmente:

| Herramienta | Versión mínima | Uso |
|---|---|---|
| [Java (JDK)](https://adoptium.net/) | 21 | Compilación y ejecución del servicio |
| [Docker](https://www.docker.com/) / Docker Compose | 24+ | Base de datos PostgreSQL y contenedor de la aplicación |
| [Git](https://git-scm.com/) | 2.x | Control de versiones |
| Maven Wrapper (`mvnw`, incluido en el repo) | — | No requiere instalación de Maven local |

Para trabajar en la documentación:

| Herramienta | Versión mínima | Uso |
|---|---|---|
| [Python](https://www.python.org/) | 3.9+ | Requerido por MkDocs |
| [MkDocs](https://www.mkdocs.org/) + [Material for MkDocs](https://squidfunk.github.io/mkdocs-material/) | — | Generación del sitio de documentación |

Ver [Configuración](configuracion.md) para los pasos de instalación de cada
herramienta y las variables de entorno del servicio.
