# Introducción

## Contexto

**TechCup Fútbol** es un torneo universitario cuya plataforma digital, **Astro
Merge**, está compuesta por alrededor de 12 microservicios independientes,
cada uno responsable de un dominio de negocio acotado (identidad, torneos,
partidos, estadísticas, notificaciones, logística, etc.).

El **Servicio de Logística** (`service-logistics`) es el microservicio
encargado de la operación **no deportiva** del torneo: qué refrigerio le
corresponde a cada equipo, qué se entregó realmente, y qué dotación (petos,
balones, kits) se le entregó a cada árbitro.

## Propósito

Darle al Organizador una herramienta confiable para planear y registrar la
logística del día del torneo, de forma que:

- Quede claro qué refrigerio corresponde a cada equipo en cada partido, sin
  duplicar definiciones.
- Cada entrega real (refrigerio o dotación) quede registrada con fecha,
  responsable y destinatario, sin permitir duplicados.
- Cada ítem de dotación tenga un estado verificable (`PENDIENTE` /
  `ENTREGADO`) y trazabilidad de quién lo entregó y quién lo recibió.
- Toda entrega se reporte al Servicio de Auditoría para trazabilidad
  posterior, sin bloquear el flujo del Organizador si esa integración falla.

## Actor único: el Organizador

Este servicio tiene un solo tipo de usuario final con permisos de escritura:
el **Organizador**, autenticado a través del API Gateway de la plataforma.
Los endpoints de solo lectura (`GET`) son accesibles para cualquier usuario
autenticado.

## Alcance

### Qué SÍ hace este servicio

1. Definir qué refrigerio(s) corresponden a un equipo en un partido
   (Requerimiento 1).
2. Registrar la entrega real de un refrigerio a un equipo o a un jugador
   (Requerimiento 2).
3. Registrar y controlar la entrega de dotación (petos, balones, kits) a
   árbitros, con su devolución (Requerimiento 3).
4. Reportar cada entrega registrada al Servicio de Auditoría, de forma
   best-effort (no bloqueante).

### Qué NO hace (responsabilidad de otros servicios)

| Responsabilidad | Servicio dueño |
|---|---|
| Partidos, alineaciones y resultados | Servicio de Partidos / Servicio de Torneos |
| Validación de equipos y jugadores | Servicio de Equipos |
| Registro de auditoría centralizado | Servicio de Auditoría |
| Autenticación y validación de firma del JWT | API Gateway |

Ver [Arquitectura](arquitectura.md) para el detalle de cómo este servicio se
comunica con cada uno de ellos.
