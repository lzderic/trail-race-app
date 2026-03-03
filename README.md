# Trail Race Application — Backend

Event-driven CQRS microservice backend for a trail race organiser.

## Architecture

```
Client  →  command-service (port 8081)  →  RabbitMQ  →  query-service (port 8082)
                    ↓                                           ↓
              PostgreSQL                                  PostgreSQL
           (trailrace_command)                         (trailrace_query)
```

| Service | Port | Responsibility |
|---|---|---|
| `race_application_command_service` | 8081 | Mutating API (CREATE / PATCH / DELETE) + event publishing |
| `race_application_query_service` | 8082 | Read API (GET) + event consumption / DB persistence |
| PostgreSQL | 5432 | Two logical databases: `trailrace_command`, `trailrace_query` |
| RabbitMQ | 5672 / 15672 | Message broker (management UI on 15672) |

## Prerequisites

- Docker & Docker Compose v2
- JDK 21 (for local development / tests)
- Maven 3.9+ (for local development / tests)

## Quick Start

```bash
# 1. Copy environment config
cp .env.example .env

# 2. Build and start everything
make up

# 3. Check logs
make logs
```

The services start after PostgreSQL and RabbitMQ are healthy (health-checks built into compose).

## API Documentation (Swagger UI)

Interactive API docs are available at runtime — no auth required to open the UI.

| Service | Swagger UI | OpenAPI JSON |
|---|---|---|
| Command Service (port 8081) | http://localhost:8081/swagger-ui.html | http://localhost:8081/v3/api-docs |
| Query Service (port 8082) | http://localhost:8082/swagger-ui.html | http://localhost:8082/v3/api-docs |

### How to use Swagger UI with JWT
1. Open the Swagger UI URL above
2. Call `POST /api/auth/login` with your credentials (see seed users below)
3. Copy the `token` value from the response
4. Click the **Authorize 🔒** button (top right)
5. Paste the token and click **Authorize**
6. All secured endpoints will now send `Authorization: Bearer <token>` automatically

## Health Endpoints

Both services expose Spring Boot Actuator health endpoints — **no authentication required**.

| Service | Health URL | Info URL |
|---|---|---|
| Command (port 8081) | http://localhost:8081/actuator/health | http://localhost:8081/actuator/info |
| Query (port 8082) | http://localhost:8082/actuator/health | http://localhost:8082/actuator/info |

### Sample health response
```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP", "details": { "database": "PostgreSQL" } },
    "diskSpace": { "status": "UP" },
    "rabbit": { "status": "UP", "details": { "version": "3.x.x" } }
  }
}
```
- `status: UP` — all components healthy
- `status: DOWN` — at least one component (DB / RabbitMQ) is unavailable

> These endpoints are also used as Docker Compose health-checks to ensure services only start after the infrastructure is ready.

## Postman Collection

A ready-to-use Postman collection is included in the repository.

**File:** `postman/Trail-Race-App.postman_collection.json`

### Import into Postman
1. Open Postman
2. Click **File → Import** (or drag-and-drop the file)
3. Select `postman/Trail-Race-App.postman_collection.json`
4. The collection appears in the left sidebar under **Trail Race App**

### Collection variables (pre-configured)

| Variable | Default | Description |
|---|---|---|
| `command_url` | `http://localhost:8081` | Command service base URL |
| `query_url` | `http://localhost:8082` | Query service base URL |
| `jwt_token` | *(empty)* | Auto-populated after a Login request |
| `race_id` | *(empty)* | Auto-populated after Create Race |
| `application_id` | *(empty)* | Auto-populated after Create Application |

### Recommended request order

```
1. Health & Info / Health — Command Service   ← verify services are up
2. Health & Info / Health — Query Service
3. Auth / Login as Administrator (Command)    ← sets {{jwt_token}}
4. Races (Command) / Create Race              ← sets {{race_id}}
5. Applications (Command) / Create Application    ← sets {{application_id}}
6. Races (Query) / Get All Races
7. Races (Query) / Get Race by ID
8. Applications (Query) / Get All Applications
9. Applications (Query) / Get Application by ID
10. Races (Command) / Update Race (PATCH)
11. Applications (Command) / Delete Application
12. Races (Command) / Delete Race
```

> **Note:** The Login request uses a [Postman test script](https://learning.postman.com/docs/writing-scripts/test-scripts/) to automatically save the token into `{{jwt_token}}`. All other requests inherit the Bearer token from the collection-level auth, so you never need to copy-paste it manually.

## API Reference

### Authentication (both services)

```
POST /api/auth/login
Content-Type: application/json

{ "email": "admin@trailrace.com", "password": "admin123" }
```

Returns `{ "token": "<JWT>" }`. Include the token as `Authorization: Bearer <JWT>` on subsequent requests.

### Seed credentials

| Email | Password | Role |
|---|---|---|
| `admin@trailrace.com` | `admin123` | ADMINISTRATOR |
| `jane@example.com` | `password` | APPLICANT |

### Command Service (port 8081) — Administrator only

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/races` | Create a race |
| `PATCH` | `/api/races/{id}` | Update a race |
| `DELETE` | `/api/races/{id}` | Delete a race |
| `POST` | `/api/applications` | Create an application |
| `DELETE` | `/api/applications/{id}` | Delete an application |

**Race payload:**
```json
{ "name": "Mountain Classic", "distance": "MARATHON" }
```
Distance values: `FIVE_K`, `TEN_K`, `HALF_MARATHON`, `MARATHON`

**Application payload:**
```json
{ "firstName": "John", "lastName": "Doe", "club": "Trail Blazers", "raceId": "<uuid>" }
```

### Query Service (port 8082) — Any authenticated user

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/races` | List all races |
| `GET` | `/api/races/{id}` | Get a race by ID |
| `GET` | `/api/applications` | List all applications |
| `GET` | `/api/applications/{id}` | Get an application by ID |

## Makefile Commands

| Command | Description |
|---|---|
| `make help` | Show all commands |
| `make build` | Build both service JARs (skip tests) |
| `make build-command` | Build command service JAR only |
| `make build-query` | Build query service JAR only |
| `make up` | Build Docker images & start all services |
| `make down` | Stop all containers |
| `make restart` | Stop then start all containers |
| `make logs` | Follow logs for all services |
| `make logs-command` | Follow command service logs |
| `make logs-query` | Follow query service logs |
| `make infra-up` | Start only PostgreSQL + RabbitMQ (no app services) |
| `make infra-down` | Stop PostgreSQL + RabbitMQ |
| `make test` | Run all unit tests |
| `make test-command` | Run command service tests only |
| `make test-query` | Run query service tests only |
| `make clean` | Clean Maven build artifacts |
| `make clean-docker` | Remove containers + volumes (destructive) |

## Configuration

All configuration values can be overridden via environment variables (see `.env.example`):

| Variable | Default | Description |
|---|---|---|
| `POSTGRES_USER` | `trailrace` | PostgreSQL username |
| `POSTGRES_PASSWORD` | `trailrace` | PostgreSQL password |
| `POSTGRES_PORT` | `5432` | PostgreSQL host port |
| `RABBITMQ_USERNAME` | `guest` | RabbitMQ username |
| `RABBITMQ_PASSWORD` | `guest` | RabbitMQ password |
| `RABBITMQ_PORT` | `5672` | RabbitMQ AMQP port |
| `RABBITMQ_MANAGEMENT_PORT` | `15672` | RabbitMQ management UI port |
| `COMMAND_SERVICE_PORT` | `8081` | Command service host port |
| `QUERY_SERVICE_PORT` | `8082` | Query service host port |
| `JWT_SECRET` | *(long default)* | HMAC-SHA256 signing secret — **change in production** |
| `JWT_EXPIRATION_MS` | `86400000` | Token expiry in milliseconds (24 h) |

## Database Migrations

Flyway runs automatically on service startup.

- **Command service** migrations: `backend/race_application_command_service/src/main/resources/db/migration/`
  - `V1` — users table
  - `V2` — races table
  - `V3` — applications table (FK → races)
  - `V4` — seed users (`admin@trailrace.com` / `jane@example.com`)

- **Query service** migrations: `backend/race_application_query_service/src/main/resources/db/migration/`
  - `V1`–`V4` — same schema without FK constraints (read-side replica, populated via RabbitMQ events)

## RabbitMQ Topology

- **Exchange**: `trail.race.exchange` (topic)
- **Queues**: `race.queue`, `application.queue`
- **Routing keys**:

| Routing key | Trigger |
|---|---|
| `race.created` | New race created |
| `race.updated` | Race patched |
| `race.deleted` | Race deleted |
| `application.created` | New application created |
| `application.deleted` | Application deleted |

## Project Structure

```
trail-race-app/
├── backend/
│   ├── race_application_command_service/   # Write-side (port 8081)
│   │   ├── src/main/java/com/race/command/
│   │   │   ├── config/          # Security, RabbitMQ, OpenAPI config
│   │   │   ├── controller/      # REST controllers
│   │   │   ├── dto/             # Request / response records
│   │   │   ├── entity/          # JPA entities
│   │   │   ├── enums/           # Distance, Role, EventType
│   │   │   ├── event/           # Event payload classes
│   │   │   ├── exception/       # GlobalExceptionHandler
│   │   │   ├── publisher/       # RabbitMQ event publisher
│   │   │   ├── repository/      # Spring Data JPA repos
│   │   │   ├── security/        # JWT filter + service
│   │   │   └── service/         # Business logic
│   │   └── src/main/resources/db/migration/  # Flyway scripts
│   │
│   └── race_application_query_service/     # Read-side (port 8082)
│       ├── src/main/java/com/race/query/
│       │   ├── config/          # Security, RabbitMQ, OpenAPI config
│       │   ├── controller/      # REST controllers
│       │   ├── dto/             # Response records
│       │   ├── entity/          # JPA entities
│       │   ├── enums/           # Distance, Role, EventType
│       │   ├── event/           # Event payload classes
│       │   ├── exception/       # GlobalExceptionHandler
│       │   ├── listener/        # RabbitMQ event listener
│       │   ├── repository/      # Spring Data JPA repos
│       │   ├── security/        # JWT filter + service
│       │   └── service/         # Business logic
│       └── src/main/resources/db/migration/  # Flyway scripts
│
├── scripts/
│   └── init-multiple-dbs.sh    # Creates trailrace_command + trailrace_query in Postgres
├── postman/
│   └── Trail-Race-App.postman_collection.json  # Import into Postman for all endpoints
├── docker-compose.yml
├── Makefile
├── .env.example
└── README.md
```

## Known Gaps & Improvements

- JWT is stateless — no token revocation (add Redis-backed denylist for production)
- Shared JWT secret between services — consider a dedicated Auth Service with JWKS endpoint
- No pagination on GET-all endpoints
- No HTTPS termination — add a reverse proxy (Nginx/Traefik) in front
- `users` table is duplicated in both DBs — in production use a shared identity service
- No dead-letter queue (DLQ) configured for failed message processing
- `@MockBean` is deprecated in Spring Boot 3.4 — migrate to `@MockitoBean` in future
