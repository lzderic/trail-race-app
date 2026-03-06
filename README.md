# Trail Race Application

Full-stack event-driven CQRS application for a trail race organiser.

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

- Docker & Docker Compose v2 *(for containerised run)*
- JDK 21
- Maven 3.9+
- Node.js 18+ & npm 9+ *(for the frontend)*

## Quick Start — Docker (all services)

```bash
# 1. Copy environment config
cp .env.example .env

# 2. Build images and start everything (Postgres, RabbitMQ, backend services, frontend)
make up                       # or: docker compose up --build -d

# 3. Open the app
#    Frontend:    http://localhost:3000
#    Swagger UI:  http://localhost:8081/swagger-ui.html  (command)
#                 http://localhost:8082/swagger-ui.html  (query)
#    RabbitMQ:    http://localhost:15672  (guest / guest)

# 4. Tail logs
make logs
```

> Services start after PostgreSQL and RabbitMQ are healthy (health-checks built into compose).

## Quick Start — Local Development (no Docker for services)

Use this when you want to run the backend services and frontend **outside Docker** and only use Docker for infrastructure (Postgres + RabbitMQ).

### 1. Start infrastructure

```bash
make infra-up                 # starts Postgres (port 5432) + RabbitMQ (port 5672)
```

Or if you already have Postgres and RabbitMQ running locally, skip this step.

### 2. Build the shared database module

The shared Flyway migrations JAR must be installed to your local Maven repo **before** the services can start:

```bash
make build-shared
# or: cd backend/shared-db-migrations && ./mvnw clean install -DskipTests
```

### 3. Start the backend services

Run each service in its own terminal:

**Command Service (port 8081):**
```bash
cd backend/race_application_command_service
./mvnw spring-boot:run        # Linux / macOS
mvnw.cmd spring-boot:run      # Windows
```

**Query Service (port 8082):**
```bash
cd backend/race_application_query_service
./mvnw spring-boot:run        # Linux / macOS
mvnw.cmd spring-boot:run      # Windows
```

> Both services default to `localhost:5432` for Postgres and `localhost:5672` for RabbitMQ.
> Flyway runs automatically on startup and creates all tables + seed data.

> **IntelliJ IDEA users:** The repo includes shared run configurations (`.idea/runConfigurations/`).
> Open the project and you'll find ready-to-use configs like *Command Service*, *Query Service*,
> *Both Services*, *Full Stack*, *Frontend Dev*, *Storybook Dev*, etc. in the Run menu.

### 4. Start the frontend (port 3000)

```bash
cd frontend
npm install
npm run dev
```

The Vite dev server starts on **http://localhost:3000** and proxies `/api` requests to the command service (`localhost:8081`). The query service is called directly from the browser on port 8082.

### 5. Open the application

| What | URL |
|---|---|
| Frontend | http://localhost:3000 |
| Command Swagger | http://localhost:8081/swagger-ui.html |
| Query Swagger | http://localhost:8082/swagger-ui.html |
| RabbitMQ Management | http://localhost:15672 *(guest / guest)* |

## API Documentation (Swagger UI)

Interactive API docs are available at runtime — no auth required to open the UI.
Swagger UI includes embedded documentation with seed credentials, endpoint descriptions, and request/response schemas.

| Service | Swagger UI | OpenAPI JSON |
|---|---|---|
| Command Service (port 8081) | http://localhost:8081/swagger-ui.html | http://localhost:8081/v3/api-docs |
| Query Service (port 8082) | http://localhost:8082/swagger-ui.html | http://localhost:8082/v3/api-docs |

### How to use Swagger UI with JWT
1. Open one of the Swagger UI URLs above
2. Expand **auth-controller** and call `POST /api/auth/login` with credentials (see [Seed credentials](#seed-credentials) below)
3. Copy the `token` value from the response
4. Click the **Authorize 🔒** button (top right of the page)
5. Paste the token into the **Value** field and click **Authorize**
6. All secured endpoints will now include `Authorization: Bearer <token>` automatically
7. Try any endpoint — click **Try it out**, fill in parameters, and click **Execute**

> **Tip:** The Swagger description page itself lists the seed credentials and available roles, so you don't need to leave the browser.

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
| `jwt_token_applicant` | *(empty)* | Auto-populated after Login as Applicant |
| `race_id` | *(empty)* | Auto-populated after Create Race |
| `application_id` | *(empty)* | Auto-populated after Create Application |

### Recommended request order

```
 1. Health & Info / Health — Command Service        ← verify services are up
 2. Health & Info / Health — Query Service
 3. Auth / Login as Administrator (Command)         ← sets {{jwt_token}}
 4. Races (Command) / Create Race                   ← sets {{race_id}}
 5. Applications (Command) / Create Application     ← sets {{application_id}}
 6. Races (Query) / Get All Races
 7. Races (Query) / Get Race by ID
 8. Applications (Query) / Get All Applications
 9. Applications (Query) / Get Application by ID
10. Races (Command) / Update Race (PATCH)
11. Auth / Login as Applicant (Command)             ← sets {{jwt_token_applicant}}
12. Applications (Command) / Create Application (as Applicant)
13. Races (Command) / Create Race — Forbidden (403) ← negative test
14. Auth / Login — Invalid Credentials (401)        ← negative test
15. Races (Command) / Delete Race — Not Found (404) ← negative test
16. Applications (Command) / Delete Application
17. Races (Command) / Delete Race
```

> **Note:** Login requests use Postman test scripts to automatically save tokens. All other requests inherit the Bearer token from the collection-level auth.
>
> **CQRS note:** After a write (command service), the query service may need a moment to process the RabbitMQ event. If a GET returns stale data, wait briefly and retry.

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
| `jane@example.com` | `applicant123` | APPLICANT |
| `marco@example.com` | `applicant123` | APPLICANT |
| `lena@example.com` | `applicant123` | APPLICANT |

### Seed races (pre-loaded)

| Name | Distance |
|---|---|
| Mountain Trail 5K | 5k |
| Forest Loop 10K | 10k |
| Coastal Half Marathon | HalfMarathon |
| Alpine Marathon | Marathon |
| City Park 5K | 5k |
| River Valley 10K | 10k |

### Command Service (port 8081)

| Method | Path | Role | Description |
|---|---|---|---|
| `POST` | `/api/races` | Administrator | Create a race |
| `PATCH` | `/api/races/{id}` | Administrator | Update a race |
| `DELETE` | `/api/races/{id}` | Administrator | Delete a race |
| `POST` | `/api/applications` | Administrator, Applicant | Create an application |
| `DELETE` | `/api/applications/{id}` | Administrator, Applicant | Delete an application |

**Race payload:**
```json
{ "name": "Mountain Classic", "distance": "MARATHON" }
```
Distance values: `FIVE_K`, `TEN_K`, `HALF_MARATHON`, `MARATHON`

**Application payload:**
```json
{ "firstName": "John", "lastName": "Doe", "club": "Trail Blazers", "raceId": "<uuid>" }
```

**Application response** includes `userId` (auto-set from the authenticated user's JWT):
```json
{ "id": "<uuid>", "firstName": "John", "lastName": "Doe", "club": "Trail Blazers", "raceId": "<uuid>", "userId": "<uuid>" }
```

### Query Service (port 8082) — Any authenticated user

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/races` | List all races |
| `GET` | `/api/races/{id}` | Get a race by ID |
| `GET` | `/api/applications` | List all applications |
| `GET` | `/api/applications/{id}` | Get an application by ID |

## Frontend Application Guide

The React SPA (Vite + TypeScript + Redux Toolkit) runs on **http://localhost:3000**.

### Pages & Routes

| Route | Access | Description |
|---|---|---|
| `/login` | Public | Login form (email + password) |
| `/` | Any authenticated user | List of all races — home page |
| `/applications` | Any authenticated user | List of the current user's applications |
| `/admin/races` | Administrator only | Manage races (add, edit, delete) |
| `/admin/applications` | Administrator only | View all applications |
| `/404` | Public | Not Found page |

### User flows

**As an Applicant:**
1. Log in with one of the applicant credentials (e.g. `jane@example.com` / `applicant123`)
2. You see the **Races** page (home) — a list of all available races
3. Click **Apply** on any race → a modal opens with your first/last name pre-filled (read-only from your account)
4. Optionally fill in a club name, then submit
5. Navigate to **My Applications** to see your applications
6. Click **Delete** to remove an application

**As an Administrator:**
1. Log in with `admin@trailrace.com` / `admin123`
2. You see the **Races** page (home) — same list of races
3. Navigate to **Admin > Races** to create new races, edit existing ones (name/distance), or delete them
4. Navigate to **Admin > Applications** to view all applications across all users

### Technology stack

| Layer | Library |
|---|---|
| UI framework | React 18 |
| Build tool | Vite 6 |
| State management | Redux Toolkit |
| Routing | React Router v6 |
| HTTP client | Axios (separate instances for command & query services) |
| Testing | Vitest + React Testing Library |
| Component explorer | Storybook 10 |
| Linting | ESLint 9 (flat config) |
| Formatting | Prettier |
| Language | TypeScript 5 |

## Makefile Commands

| Command | Description |
|---|---|
| `make help` | Show all commands |
| **Build** | |
| `make build` | Build all services (backend JARs + frontend) |
| `make build-shared` | Install the shared-db-migrations module to local Maven repo |
| `make build-backend` | Build all backend modules via aggregator POM |
| `make build-command` | Build command service JAR only |
| `make build-query` | Build query service JAR only |
| `make build-frontend` | Build the frontend for production |
| **Docker** | |
| `make up` | Build Docker images & start all services |
| `make down` | Stop all containers |
| `make restart` | Stop then start all containers |
| `make logs` | Follow logs for all services |
| `make logs-command` | Follow command service logs |
| `make logs-query` | Follow query service logs |
| `make logs-frontend` | Follow frontend container logs |
| `make infra-up` | Start only PostgreSQL + RabbitMQ (no app services) |
| `make infra-down` | Stop PostgreSQL + RabbitMQ |
| **Frontend** | |
| `make frontend-install` | Install frontend npm dependencies |
| `make frontend-dev` | Start the frontend dev server (hot reload, port 3000) |
| `make frontend-test` | Run frontend unit tests (Vitest) |
| **Tests** | |
| `make test` | Run all unit tests (backend + frontend) |
| `make test-command` | Run command service tests only |
| `make test-query` | Run query service tests only |
| **Clean** | |
| `make clean` | Clean Maven build artifacts + frontend dist |
| `make clean-docker` | Remove containers + volumes (destructive) |
| `make db-reset` | Drop and recreate both databases (requires running Postgres container) |
| **Storybook** | |
| `make storybook` | Start Storybook dev server (port 6006) |
| `make storybook-build` | Build Storybook static site |
| `make storybook-docker` | Start Storybook in Docker (port 6006) |

## IntelliJ IDEA Run Configurations

The repository includes pre-configured IntelliJ run configurations in `.idea/runConfigurations/`. After opening the project, they appear in the **Run / Debug** dropdown:

| Configuration | What it does |
|---|---|
| **Command Service** | Starts the command service (port 8081) |
| **Query Service** | Starts the query service (port 8082) |
| **Full Stack** | Starts both services + frontend dev server |
| **Frontend Dev** | Runs `npm run dev` (port 3000) |
| **Frontend Build** | Runs `npm run build` |
| **Frontend Lint** | Runs `npm run lint` (ESLint) |
| **Frontend Preview** | Runs `npm run preview` |
| **Frontend Tests** | Runs `npm test` (single run) |
| **Frontend Tests (Watch)** | Runs `npm run test:watch` |
| **Command Service Tests** | Runs `mvnw test` for command service |
| **Query Service Tests** | Runs `mvnw test` for query service |
| **Command: mvn clean package** | Builds command service JAR |
| **Query: mvn clean package** | Builds query service JAR |
| **Storybook Dev** | Runs `npm run storybook` (port 6006) |
| **Storybook Build** | Builds static Storybook site |
| **Docker: Infrastructure** | Starts Postgres + RabbitMQ containers |
| **Docker: All Services** | Starts all Docker containers |

## Storybook

[Storybook](https://storybook.js.org/) lets you browse and interact with UI components in isolation, without needing the backend running.

### Start Storybook

```bash
# Development (hot reload)
cd frontend && npm run storybook     # or: make storybook
# → Opens http://localhost:6006 in your browser

# Build static site (for deployment / CI)
cd frontend && npm run build-storybook   # or: make storybook-build
# → Output in frontend/storybook-static/

# Run in Docker (port 6006)
docker compose --profile storybook up --build -d storybook   # or: make storybook-docker
```

### How to use Storybook
1. Start Storybook with one of the commands above
2. Open **http://localhost:6006** in your browser
3. Browse components in the left sidebar — each component has multiple "stories" showing different states
4. Use the **Controls** panel (bottom) to tweak props interactively
5. Use the **Actions** panel to see event callbacks (e.g. button clicks)

### Available stories

| Story | Components / Variants |
|---|---|
| Common / Alert | Error, Success, Info, WithCloseButton, LongMessage |
| Common / ConfirmDialog | Default, CustomLabels, Loading |
| Common / Spinner | Default |
| Modals / RaceFormModal | CreateNew, EditExisting |
| Modals / ApplyToRaceModal | FiveK, Marathon, HalfMarathon |
| Pages / LoginPage | Default |

## Configuration

All configuration values can be overridden via environment variables (see `.env.example` at the project root and `frontend/.env.example` for the React app):

### Root `.env.example` (used by docker-compose & backend services)

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
| `FRONTEND_PORT` | `3000` | Frontend host port (Docker only — maps to Nginx port 80) |
| `JWT_SECRET` | *(long default)* | HMAC-SHA256 signing secret — **change in production** |
| `JWT_EXPIRATION_MS` | `86400000` | Token expiry in milliseconds (24 h) |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000` | Allowed CORS origins for the backend |
| `STORYBOOK_PORT` | `6006` | Storybook host port (Docker only) |

### Frontend `frontend/.env.example` (used only for Docker / production builds)

| Variable | Default | Description |
|---|---|---|
| `VITE_COMMAND_API_URL` | `http://localhost:8081` | Command service URL embedded at build time |
| `VITE_QUERY_API_URL` | `http://localhost:8082` | Query service URL embedded at build time |

> **Local dev note:** When running the frontend with `npm run dev`, Vite proxies `/api` calls to `localhost:8081` (configured in `vite.config.ts`), so these variables are not needed.

## Database Migrations

Flyway runs automatically on service startup. Shared migration scripts (users, races, seed
data) live in a dedicated Maven module to avoid duplication.

### Shared module: `backend/shared-db-migrations`

A plain JAR containing SQL scripts under `db/migration/shared/`:

| Version | Script | Description |
|---|---|---|
| V1 | `V1__create_users_table.sql` | Create `users` table (with `created_at`, `updated_at`) |
| V2 | `V2__create_races_table.sql` | Create `races` table (with `created_at`, `updated_at`) |
| V4 | `V4__seed_users.sql` | Seed 1 admin + 3 applicant users |
| V5 | `V5__seed_races.sql` | Seed 6 sample races (all distances) |

Both services depend on this module (`com.race:shared-db-migrations`) and Flyway scans
`classpath:db/migration/shared` (from the JAR) plus `classpath:db/migration` (service-specific).

> **First-time setup:** run `make build-shared` (or `cd backend/shared-db-migrations && ./mvnw install -DskipTests`)
> before starting or building the services locally.

### Service-specific migrations

- **Command service** `src/main/resources/db/migration/`
  - `V3` — applications table (with `user_id`, `created_at`, `updated_at`)

- **Query service** `src/main/resources/db/migration/`
  - `V3` — applications table (with `user_id`, `created_at`, `updated_at` — read-side replica, populated via RabbitMQ events)

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
├── .env.example                            # Environment variable template
├── .gitignore                              # Root gitignore
├── docker-compose.yml
├── Makefile
├── README.md
│
├── backend/
│   ├── pom.xml                             # Aggregator POM (multi-module)
│   ├── shared-db-migrations/               # Shared Flyway SQL scripts (JAR module)
│   │   ├── pom.xml
│   │   └── src/main/resources/db/migration/shared/
│   │       ├── V1__create_users_table.sql
│   │       ├── V2__create_races_table.sql
│   │       ├── V4__seed_users.sql
│   │       └── V5__seed_races.sql
│   │
│   ├── race_application_command_service/    # Write-side (port 8081)
│   │   ├── Dockerfile
│   │   ├── pom.xml
│   │   ├── src/main/java/com/race/command/
│   │   │   ├── config/          # Security, RabbitMQ, OpenAPI config
│   │   │   ├── controller/      # REST controllers (+ AuthController)
│   │   │   ├── dto/             # Request / response records
│   │   │   ├── entity/          # JPA entities
│   │   │   ├── enums/           # Distance, Role, EventType
│   │   │   ├── event/           # Event payload classes
│   │   │   ├── exception/       # GlobalExceptionHandler
│   │   │   ├── publisher/       # RabbitMQ event publisher
│   │   │   ├── repository/      # Spring Data JPA repos
│   │   │   ├── security/        # JWT filter + service
│   │   │   └── service/         # Business logic
│   │   ├── src/main/resources/db/migration/  # Service-specific Flyway scripts
│   │   └── src/test/            # Unit tests
│   │
│   └── race_application_query_service/     # Read-side (port 8082)
│       ├── Dockerfile
│       ├── pom.xml
│       ├── src/main/java/com/race/query/
│       │   ├── config/          # Security, RabbitMQ, OpenAPI config
│       │   ├── controller/      # REST controllers (+ AuthController)
│       │   ├── dto/             # Response records
│       │   ├── entity/          # JPA entities
│       │   ├── enums/           # Distance, Role, EventType
│       │   ├── event/           # Event payload classes
│       │   ├── exception/       # GlobalExceptionHandler
│       │   ├── listener/        # RabbitMQ event listener
│       │   ├── repository/      # Spring Data JPA repos
│       │   ├── security/        # JWT filter + service
│       │   └── service/         # Business logic
│       ├── src/main/resources/db/migration/  # Service-specific Flyway scripts
│       └── src/test/            # Unit tests
│
├── frontend/                               # React SPA (Vite + TypeScript + Redux Toolkit)
│   ├── .storybook/              # Storybook config
│   ├── Dockerfile               # Production build (Nginx)
│   ├── Dockerfile.storybook     # Storybook Docker build
│   ├── nginx.conf               # Nginx config for SPA routing
│   ├── vite.config.ts
│   ├── eslint.config.js
│   ├── .prettierrc
│   └── src/
│       ├── api/                 # Axios API clients
│       ├── components/          # Reusable UI components (+ stories)
│       ├── pages/               # Route pages (Login, Races, Applications, Admin)
│       ├── store/               # Redux store + slices
│       ├── stories/             # Storybook stories
│       ├── test/                # Vitest unit tests
│       ├── types/               # TypeScript types
│       └── utils/               # Helpers (JWT decode, distance mapping)
│
├── scripts/
│   ├── init-multiple-dbs.sh     # Creates trailrace_command + trailrace_query in Postgres
│   └── reset_flyway_history.sql # Utility to reset Flyway + drop all tables
│
└── postman/
    └── Trail-Race-App.postman_collection.json  # Full Postman collection
```

## Running Tests

### All tests (backend + frontend)
```bash
make test
```

### Backend only
```bash
make test-command      # command service unit tests
make test-query        # query service unit tests
```

### Frontend only
```bash
cd frontend
npm test               # single run (Vitest)
npm run test:watch     # watch mode
```

Or via the Makefile:
```bash
make frontend-test
```

## Linting & Formatting

### Frontend

**ESLint** (code quality) and **Prettier** (code formatting) are configured:

```bash
cd frontend
npm run lint           # run ESLint on src/
npm run format         # auto-format with Prettier
npm run format:check   # check formatting without writing (useful in CI)
```

Configuration files:
- `frontend/eslint.config.js` — ESLint flat config (ESLint 9+ format)
- `frontend/.prettierrc` — Prettier rules
- `frontend/.prettierignore` — files excluded from formatting

## Troubleshooting

### Flyway validation error on startup
If you see `Detected applied migration not resolved locally`, your database has old migration history. Reset it:
```bash
# Via Docker:
make db-reset

# Or manually with psql:
psql -h localhost -U trailrace -d trailrace_command -f scripts/reset_flyway_history.sql
psql -h localhost -U trailrace -d trailrace_query   -f scripts/reset_flyway_history.sql
```
Then restart the services — Flyway will recreate everything.

### `shared-db-migrations` not found
Both services depend on the shared migrations JAR. If you see a Maven dependency error, install it first:
```bash
make build-shared
# or: cd backend/shared-db-migrations && ./mvnw clean install -DskipTests
```

### Port already in use
If port 5432, 5672, 8081, 8082, or 3000 is already taken, either stop the conflicting process or change the port in `.env` / `application.properties`.

### Frontend shows stale data after a write
The CQRS architecture is eventually consistent. After a write to the command service (port 8081), the query service (port 8082) receives the event via RabbitMQ asynchronously. If a GET returns stale data, wait a moment and refresh.

