# Lottery API

API REST construida con **Spring Boot 3.4 / Java 21** para análisis estadístico de los juegos de pronósticos de Lotería Nacional de México: **Melate**, **Revancha**, **Revanchita** y **Gana Gato**.

---

## Requisitos

| Herramienta | Versión mínima | Notas |
|---|---|---|
| Java (JDK) | 21 | Verificar con `java -version` |
| Gradle | 8.x o 9.x | Instalado vía Homebrew o sdkman |
| Docker Desktop | Cualquier reciente | Necesario para PostgreSQL y tests de integración |

---

## Levantar el entorno

### 1. Iniciar la base de datos

```bash
docker compose up -d
```

Esto levanta:
- **PostgreSQL 17** en `localhost:5432` (base de datos `lottery_db`)
- **PgAdmin 4** en `http://localhost:5050` (usuario `admin@lottery.com` / contraseña `admin`)

Flyway ejecuta las migraciones automáticamente al iniciar la app.

### 2. Iniciar la aplicación

```bash
gradle bootRun
```

La app arranca en `http://localhost:8080`.

---

## Ejecutar los tests

```bash
gradle test
```

- Los **tests unitarios** usan mocks (Mockito), no requieren Docker.
- Los **tests de integración** (`*IT.java`) levantan un contenedor PostgreSQL real con **Testcontainers** — Docker debe estar corriendo.
- Se genera reporte de cobertura JaCoCo en `build/reports/jacoco/test/html/index.html` (mínimo 90 % en servicios y adaptadores).

---

## Documentación interactiva (Swagger UI)

Con la app corriendo:

```
http://localhost:8080/swagger-ui.html
```

Todos los endpoints están documentados con descripción, parámetros y ejemplos de respuesta.

---

## Endpoints de la API

Base URL: `http://localhost:8080/api/v1/lottery`

Los tipos válidos para `{type}` son: `MELATE`, `REVANCHA`, `REVANCHITA`, `GANA_GATO`

### Sincronización de históricos

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/{type}/sync` | Descarga e inserta el histórico oficial desde Lotería Nacional |
| `POST` | `/sync/all` | Sincroniza los 4 tipos en secuencia |
| `POST` | `/{type}/import` | Importa un CSV descargado manualmente (`multipart/form-data`) |

**Ejemplo — sincronizar Melate:**
```bash
curl -X POST http://localhost:8080/api/v1/lottery/MELATE/sync
```

**Respuesta:**
```json
{
  "lotteryType": "MELATE",
  "totalRecords": 3124,
  "newRecords": 3124,
  "skippedRecords": 0,
  "status": "SUCCESS",
  "message": "Sincronización completada",
  "syncedAt": "2026-05-01T12:00:00"
}
```

**Importar CSV local:**
```bash
curl -X POST http://localhost:8080/api/v1/lottery/MELATE/import \
  -F "file=@/ruta/al/archivo.csv"
```

---

### Estadísticas

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/{type}/statistics` | Estadísticas completas (frecuencias, top 10, nunca sorteados) |
| `GET` | `/{type}/statistics?from=YYYY-MM-DD&to=YYYY-MM-DD` | Idem, filtrado por rango de fechas |

**Ejemplo:**
```bash
curl http://localhost:8080/api/v1/lottery/MELATE/statistics
curl "http://localhost:8080/api/v1/lottery/MELATE/statistics?from=2024-01-01&to=2024-12-31"
```

**Respuesta parcial:**
```json
{
  "lotteryType": "MELATE",
  "totalDraws": 3124,
  "firstDrawDate": "1999-05-05",
  "lastDrawDate": "2026-04-30",
  "mostFrequent": [
    { "number": 38, "frequency": 412, "percentage": 13.18 }
  ],
  "leastFrequent": [...],
  "averageFrequency": 334.5,
  "numbersNeverDrawn": []
}
```

---

### Frecuencias de números

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/{type}/frequencies` | Frecuencia de todos los números, ordenada por número |
| `GET` | `/{type}/frequencies/{number}` | Frecuencia de un número específico |

**Ejemplos:**
```bash
curl http://localhost:8080/api/v1/lottery/MELATE/frequencies
curl http://localhost:8080/api/v1/lottery/MELATE/frequencies/7
```

---

### Números calientes y fríos

| Método | Ruta | Parámetros | Descripción |
|---|---|---|---|
| `GET` | `/{type}/hot-numbers` | `limit` (default 10) | Top N más frecuentes históricamente |
| `GET` | `/{type}/cold-numbers` | `limit` (default 10) | Top N menos frecuentes |
| `GET` | `/{type}/recent-hot-numbers` | `recentDraws` (default 20), `limit` (default 10) | Más frecuentes en los últimos N sorteos |

**Ejemplos:**
```bash
curl "http://localhost:8080/api/v1/lottery/MELATE/hot-numbers?limit=15"
curl "http://localhost:8080/api/v1/lottery/MELATE/cold-numbers?limit=5"
curl "http://localhost:8080/api/v1/lottery/MELATE/recent-hot-numbers?recentDraws=50&limit=10"
```

---

### Sugerencias estadísticas

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/{type}/suggestions` | Devuelve sugerencias con las 4 metodologías |
| `GET` | `/{type}/suggestions/{methodology}` | Sugerencia de una metodología específica |

Metodologías disponibles:

| Metodología | Descripción | Confianza |
|---|---|---|
| `HOT_NUMBERS` | Los N números con mayor frecuencia histórica | 65 % |
| `COLD_NUMBERS` | Los N menos frecuentes (teoría del retraso) | 45 % |
| `BALANCED` | Mitad calientes + mitad fríos | 55 % |
| `STATISTICAL_RANDOM` | Selección aleatoria ponderada por frecuencia | 50 % |

> **Nota:** La confianza es un indicador estadístico relativo, no una predicción. La lotería es aleatoria por definición.

**Ejemplos:**
```bash
curl http://localhost:8080/api/v1/lottery/MELATE/suggestions
curl http://localhost:8080/api/v1/lottery/MELATE/suggestions/HOT_NUMBERS
curl http://localhost:8080/api/v1/lottery/GANA_GATO/suggestions/BALANCED
```

**Respuesta:**
```json
{
  "lotteryType": "MELATE",
  "methodology": "HOT_NUMBERS",
  "suggestedNumbers": [7, 12, 23, 38, 41, 45],
  "suggestedAdditional": 19,
  "description": "Los 6 números con mayor frecuencia histórica",
  "confidenceScore": 0.65
}
```

---

## Flujo recomendado para primera ejecución

```bash
# 1. Levantar infraestructura
docker compose up -d

# 2. Iniciar la app
gradle bootRun

# 3. Sincronizar todos los históricos (aprox. 1-2 min)
curl -X POST http://localhost:8080/api/v1/lottery/sync/all

# 4. Consultar estadísticas de Melate
curl http://localhost:8080/api/v1/lottery/MELATE/statistics

# 5. Ver sugerencias para Gana Gato
curl http://localhost:8080/api/v1/lottery/GANA_GATO/suggestions
```

---

## Variables de entorno

La app acepta las siguientes variables de entorno (con sus valores por defecto):

| Variable | Default | Descripción |
|---|---|---|
| `DB_HOST` | `localhost` | Host de PostgreSQL |
| `DB_PORT` | `5432` | Puerto de PostgreSQL |
| `DB_NAME` | `lottery_db` | Nombre de la base de datos |
| `DB_USER` | `lottery_user` | Usuario |
| `DB_PASSWORD` | `lottery_pass` | Contraseña |

---

## Arquitectura

El proyecto sigue **arquitectura hexagonal (Ports & Adapters)**:

```
domain/          → Modelos de negocio y puertos (interfaces)
application/     → Casos de uso (servicios que implementan puertos de entrada)
infrastructure/  → Adaptadores: REST (web), JPA (persistence), WebClient (downloader)
```

**Regla de dependencias:** `infrastructure → application → domain`. El dominio no conoce ninguna tecnología externa.

### Tecnologías

| Componente | Tecnología |
|---|---|
| Framework | Spring Boot 3.4.1 |
| Lenguaje | Java 21 |
| Persistencia | Spring Data JPA + PostgreSQL 17 |
| Migraciones | Flyway |
| Mapeos | MapStruct 1.6 |
| Parseo CSV | OpenCSV 5.9 |
| HTTP Client | Spring WebFlux (WebClient + Reactor Netty) |
| Documentación | SpringDoc OpenAPI 3 (Swagger UI) |
| Tests | JUnit 5, Mockito, Testcontainers |
| Build | Gradle 8/9 |

---

## Estructura de la base de datos

Tabla principal: `lottery_draws`

| Columna | Tipo | Descripción |
|---|---|---|
| `id` | `BIGSERIAL` | PK |
| `lottery_type` | `VARCHAR` | `MELATE`, `REVANCHA`, `REVANCHITA`, `GANA_GATO` |
| `draw_number` | `INTEGER` | Número de concurso (único por tipo) |
| `draw_date` | `DATE` | Fecha del sorteo |
| `number_1` … `number_8` | `INTEGER` | Números del sorteo (algunos nullable según el tipo) |
| `additional_number` | `INTEGER` | Solo Melate (R7) |
| `jackpot_amount` | `NUMERIC` | Monto del pozo |
| `created_at` | `TIMESTAMP` | Fecha de inserción |

Restricción única: `(lottery_type, draw_number)` — evita duplicados en re-sincronizaciones.
