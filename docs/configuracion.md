# Configuración

## Clonar el repositorio

```bash
git clone https://github.com/TECH-CUP-2026-INT/am-logistic-service.git
cd am-logistic-service
```

## Ejecutar el servicio localmente

### Opción 1: Docker Compose (recomendado)

Levanta MongoDB y el servicio con un solo comando:

```bash
docker compose up --build
```

El servicio queda disponible en `http://localhost:8085` (puerto elegido en
`docker-compose.yml` para no chocar con `am-matches-service`, que usa `8080`,
ni con `am-notification-service`, que usa `8083`, al levantar los tres a la
vez). El MongoDB de este servicio se expone en el host en `27018` (interno
`27017`), también para evitar chocar con el de matches-service.

### Opción 2: Maven local

Requiere Java 21, Maven (o el wrapper incluido) y un MongoDB accesible (local
o Azure Cosmos DB for MongoDB vCore).

```bash
MONGODB_URI=mongodb://localhost:27017/service_logistics

./mvnw spring-boot:run
```

El servicio queda disponible en `http://localhost:8080` (puerto por defecto
fuera de Docker).

## Ejecutar con Docker (imagen suelta)

```bash
docker build -t am-logistic-service:latest .
docker run --rm -p 8080:8080 \
  -e MONGODB_URI=mongodb://host.docker.internal:27017/service_logistics \
  am-logistic-service:latest
```

## Variables de entorno

| Variable | Valor por defecto | Uso |
|---|---|---|
| `MONGODB_URI` | `mongodb://localhost:27017/service_logistics` | Connection string de MongoDB (compatible con Azure Cosmos DB for MongoDB vCore vía `mongodb://` o `mongodb+srv://`) |
| `SERVER_PORT` | `8080` | Puerto HTTP del servicio |
| `ROLE_CLAIM` | `roles` | Claim del JWT donde se buscan los roles del usuario |
| `INTERNAL_API_KEY` | `local-dev-internal-key` | API key compartida para llamadas servicio-a-servicio (debe coincidir con la de matches/notifications) |
| `TORNEOS_SERVICE_URL` | `http://localhost:8081` | Base URL del Servicio de Torneos (externo, pendiente de confirmar) |
| `EQUIPOS_SERVICE_URL` | `http://localhost:8082` | Base URL del Servicio de Equipos (externo, pendiente de confirmar) |
| `AUDITORIA_SERVICE_URL` | `http://localhost:8083` | Base URL del Servicio de Auditoría (externo, pendiente de confirmar) |

Estas variables se resuelven en `src/main/resources/application.properties`.

## Configuración adicional en `application.properties`

| Propiedad | Valor por defecto | Descripción |
|---|---|---|
| `techcup.security.role-claim` | `roles` | Claim del JWT donde se busca el rol |
| `techcup.security.internal.api-key` | `local-dev-internal-key` | API key para autenticar llamadas servicio-a-servicio |
| `management.endpoints.web.exposure.include` | `health,info` | Endpoints de Actuator expuestos |

## Documentación (MkDocs)

La documentación técnica de este servicio está construida con
[MkDocs](https://www.mkdocs.org/) y el tema
[Material for MkDocs](https://squidfunk.github.io/mkdocs-material/), igual que
en `am-matches-service`.

### Instalación

```bash
python -m venv .venv
# Linux / macOS
source .venv/bin/activate
# Windows (PowerShell)
.venv\Scripts\Activate.ps1

pip install mkdocs-material
```

### Servir la documentación en local

Desde la raíz del repositorio (donde vive `mkdocs.yml`):

```bash
mkdocs serve
```

Levanta un servidor local en
[http://127.0.0.1:8000](http://127.0.0.1:8000) con recarga automática.

### Compilar el sitio estático

```bash
mkdocs build
```

Genera el sitio en `site/` (carpeta ignorada por git), lista para publicarse
como contenido estático (por ejemplo, GitHub Pages).

### Estructura de la documentación

```
proyecto/
│
├── docs/
│   ├── index.md
│   ├── introduccion.md
│   ├── requerimientos.md
│   ├── configuracion.md
│   ├── arquitectura.md
│   ├── api.md
│   ├── pruebas.md
│   ├── equipo.md
│   ├── anexos.md
│   └── assets/
│       ├── img/
│       ├── diagrams/
│       └── stylesheets/
│           └── extra.css
│
├── mkdocs.yml
├── src/
```

Los colores y tipografía del tema (paleta morado/dorado de TechCup) están
definidos en `docs/assets/stylesheets/extra.css` y declarados en `mkdocs.yml`
bajo `extra_css` — el mismo archivo usado en `am-matches-service`, para que
los tres sitios de documentación se vean consistentes.
