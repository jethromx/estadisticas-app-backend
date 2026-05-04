# Lottery API — Pronósticos Lotería Nacional México

API REST para análisis estadístico y predicción de los juegos de pronósticos de la Lotería Nacional de México: **Melate**, **Revancha**, **Revanchita** y **GanaGato**.

---

## Tabla de Contenidos

- [Arquitectura](#arquitectura)
- [Tecnologías](#tecnologías)
- [Configuración local](#configuración-local)
- [Variables de entorno](#variables-de-entorno)
- [Migraciones de base de datos](#migraciones-de-base-de-datos)
- [Endpoints](#endpoints)
- [Seguridad](#seguridad)
- [Tests](#tests)
- [Despliegue](#despliegue)

---

## Arquitectura

El proyecto sigue **arquitectura hexagonal** (Ports & Adapters):

```
src/main/java/com/lottery/api/
├── domain/
│   ├── model/           # Entidades de dominio (SavedPrediction, LotteryDraw, User…)
│   ├── port/
│   │   ├── in/          # Puertos de entrada (casos de uso)
│   │   └── out/         # Puertos de salida (repositorios, encoders)
│   └── exception/       # Excepciones de dominio
├── application/
│   └── usecase/         # Implementaciones de casos de uso (@Service)
└── infrastructure/
    ├── adapter/
    │   ├── web/         # Controllers, DTOs, GlobalExceptionHandler
    │   ├── persistence/ # Entities JPA, Repositories, Adapters
    │   └── downloader/  # Descarga y parseo de CSV oficiales
    └── config/
        ├── security/    # JWT, Filtros, Spring Security
        └── …            # OpenAPI, WebClient
```

**Regla de dependencia estricta:**
- `domain` — sin imports de Spring, JPA ni Jackson
- `application` — solo depende de `domain` (sin imports de Spring Security)
- `infrastructure` — puede importar todo (Spring, JPA, JWT, etc.)

---

## Tecnologías

| Componente | Versión |
|---|---|
| Java | 21 |
| Spring Boot | 3.4.1 |
| Spring Security | 6.4 (JWT stateless) |
| PostgreSQL | 15+ |
| Flyway | Migraciones V1–V6 |
| Gradle | 8.x |
| JJWT | 0.12.6 |
| MapStruct | 1.6.3 |
| Springdoc OpenAPI | 2.7.0 |
| Testcontainers | 1.20.4 |

---

## Configuración local

### 1. Levantar la base de datos

```bash
docker-compose up -d
```

### 2. Compilar y ejecutar

```bash
./gradlew bootRun
```

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

---

## Variables de entorno

| Variable | Descripción | Default |
|---|---|---|
| `DB_HOST` | Host de PostgreSQL | `localhost` |
| `DB_PORT` | Puerto de PostgreSQL | `5432` |
| `DB_NAME` | Nombre de la base de datos | `lottery_db` |
| `DB_USER` | Usuario de PostgreSQL | `lottery_user` |
| `DB_PASSWORD` | Contraseña de PostgreSQL | `lottery_pass` |
| `JWT_SECRET` | Secreto HMAC-SHA256 (mín. 32 chars) | valor de desarrollo (**cambiar en prod**) |
| `JWT_EXPIRY_MS` | Tiempo de vida del token en ms | `86400000` (24h) |
| `ALLOWED_ORIGINS` | Orígenes CORS permitidos (coma-separados) | `*` |
| `PORT` | Puerto del servidor | `8080` |

---

## Migraciones de base de datos

Flyway ejecuta las migraciones automáticamente al iniciar:

| Versión | Descripción |
|---|---|
| V1 | Tabla `lottery_draws` |
| V2 | Índices de rendimiento |
| V3 | Tabla `saved_predictions` |
| V4 | Columnas `lottery_type` y `generation_params_json` en `saved_predictions` |
| V5 | Tabla `users` (registro, autenticación, roles) |
| V6 | Tabla `user_activity_log` (trazabilidad de uso) |

---

## Endpoints

### Autenticación (público — sin JWT)

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/v1/auth/register` | Registrar nuevo usuario |
| POST | `/api/v1/auth/login` | Iniciar sesión → devuelve JWT |

**Todos los demás endpoints requieren:** `Authorization: Bearer <token>`

---

### Sincronización

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/v1/lottery/{type}/sync` | Sincronizar desde CSV oficial |
| POST | `/api/v1/lottery/sync/all` | Sincronizar todos los tipos |
| GET | `/api/v1/lottery/{type}/draws` | Listar sorteos históricos |

`{type}`: `MELATE` | `REVANCHA` | `REVANCHITA` | `GANA_GATO`

---

### Análisis estadístico

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/v1/lottery/{type}/statistics` | Estadísticas generales |
| GET | `/api/v1/lottery/{type}/frequencies` | Frecuencia histórica de cada número |
| GET | `/api/v1/lottery/{type}/hot-numbers` | Números más frecuentes |
| GET | `/api/v1/lottery/{type}/due-numbers` | Números "debidos" (sobreintervalo) |
| GET | `/api/v1/lottery/{type}/windowed-frequencies` | Frecuencias en últimos N sorteos |
| GET | `/api/v1/lottery/{type}/balance` | Distribución par/impar y alto/bajo |
| GET | `/api/v1/lottery/{type}/sum-distribution` | Distribución de sumas |
| GET | `/api/v1/lottery/{type}/pairs` | Pares más co-ocurrentes |
| GET | `/api/v1/lottery/{type}/chi-square` | Prueba chi-cuadrado de uniformidad |
| GET | `/api/v1/lottery/{type}/backtest` | Backtesting de estrategias |
| GET | `/api/v1/lottery/{type}/bayesian` | Análisis bayesiano |
| GET | `/api/v1/lottery/{type}/suggestions` | Sugerencias de combinaciones |

---

### Predicciones

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/v1/predictions` | Listar predicciones del usuario autenticado |
| POST | `/api/v1/predictions` | Guardar predicción con parámetros |
| DELETE | `/api/v1/predictions/{id}` | Eliminar predicción propia |
| POST | `/api/v1/predictions/{id}/analyze` | Analizar precisión vs. sorteos reales |

**Parámetro:** `?syncFirst=true` sincroniza nuevos sorteos antes de analizar.

**Body para guardar:**
```json
{
  "label": "Mi predicción Melate enero",
  "latestDrawDate": "2025-01-15",
  "lotteryType": "MELATE",
  "combos": [[3,14,22,37,41,55], [1,5,18,29,33,47]],
  "generationParams": { "algorithm": "hot-cold-balanced", "windowSize": 50 }
}
```

---

## Seguridad

- **JWT stateless** — ningún estado de sesión en el servidor
- **Aislamiento por usuario** — cada usuario solo accede a sus predicciones
- **Trazabilidad** — cada acción autenticada se registra en `user_activity_log` de forma asíncrona
- **Ownership check** — solo el dueño puede eliminar o analizar una predicción propia
- **Endpoints públicos**: `/api/v1/auth/**`, `/actuator/health`, `/swagger-ui/**`, `/api-docs/**`

---

## Tests

```bash
# Ejecutar todos los tests (unitarios + integración)
./gradlew test

# Con reporte Jacoco
./gradlew test jacocoTestReport

# Abrir reporte HTML
open build/reports/tests/test/index.html
```

**Cobertura mínima:** 90% en clases de negocio.  
**Integración:** Testcontainers levanta PostgreSQL real (no H2).

---

## Despliegue

### Docker

```bash
docker build -t lottery-api .
docker run -p 8080:8080 \
  -e DB_HOST=<host> \
  -e JWT_SECRET=<secret-min-32-chars> \
  lottery-api
```

### Render.com

Configurado en `render.yaml`. Las variables de entorno se definen en el dashboard de Render.

---

## Swagger UI

`http://localhost:8080/swagger-ui.html`

Para autenticarse: ejecutar `POST /api/v1/auth/login` → copiar `token` → click "Authorize" → `Bearer <token>`.
