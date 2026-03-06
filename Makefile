# Trail Race Application — Makefile
# ─────────────────────────────────────────────────────────────────────────────
# Prerequisites: Docker, Docker Compose, JDK 21, Maven 3.9+, Node.js 18+
# ─────────────────────────────────────────────────────────────────────────────

COMMAND_SERVICE_DIR = backend/race_application_command_service
QUERY_SERVICE_DIR   = backend/race_application_query_service
SHARED_DB_DIR       = backend/shared-db-migrations
BACKEND_DIR         = backend
FRONTEND_DIR        = frontend

# Force TLS 1.2 for Maven downloads (works around TLS interception by some
# corporate proxies / antivirus tools that cause "bad_record_mac" errors)
export MAVEN_OPTS ?= -Dhttps.protocols=TLSv1.2 -Djdk.tls.client.protocols=TLSv1.2

.PHONY: help build build-shared build-backend build-command build-query build-frontend \
        frontend-install frontend-dev frontend-test \
        storybook storybook-build storybook-docker \
        up down restart logs logs-command logs-query logs-frontend \
        test test-command test-query \
        infra-up infra-down clean clean-docker db-reset

## ── Help ─────────────────────────────────────────────────────────────────────
help: ## Show this help message
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | \
	  awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-25s\033[0m %s\n", $$1, $$2}'

## ── Build ────────────────────────────────────────────────────────────────────
build: build-command build-query build-frontend ## Build all services (backend + frontend)

build-shared: ## Install the shared-db-migrations module to local Maven repo
	cd $(SHARED_DB_DIR) && ./mvnw clean install -DskipTests -B

build-backend: build-shared ## Build all backend modules via aggregator POM
	cd $(BACKEND_DIR) && mvn clean package -DskipTests -B

build-command: build-shared ## Build the command service JAR (installs shared module first)
	cd $(COMMAND_SERVICE_DIR) && ./mvnw clean package -DskipTests -B

build-query: build-shared ## Build the query service JAR (installs shared module first)
	cd $(QUERY_SERVICE_DIR) && ./mvnw clean package -DskipTests -B

build-frontend: frontend-install ## Build the frontend for production
	cd $(FRONTEND_DIR) && npm run build

## ── Frontend Dev ─────────────────────────────────────────────────────────────
frontend-install: ## Install frontend npm dependencies
	cd $(FRONTEND_DIR) && npm install

frontend-dev: frontend-install ## Start the frontend dev server (hot reload, port 3000)
	cd $(FRONTEND_DIR) && npm run dev

frontend-test: frontend-install ## Run frontend unit tests
	cd $(FRONTEND_DIR) && npm test

## ── Docker ───────────────────────────────────────────────────────────────────
up: ## Build Docker images and start all services (detached)
	docker compose up --build -d

down: ## Stop and remove all containers
	docker compose down

restart: down up ## Restart all containers

logs: ## Tail logs for all services
	docker compose logs -f

logs-command: ## Tail logs for the command service only
	docker compose logs -f command-service

logs-query: ## Tail logs for the query service only
	docker compose logs -f query-service

logs-frontend: ## Tail logs for the frontend container
	docker compose logs -f frontend

## ── Infrastructure only (DB + RabbitMQ, no app services) ────────────────────
infra-up: ## Start only PostgreSQL and RabbitMQ
	docker compose up -d postgres rabbitmq

infra-down: ## Stop only PostgreSQL and RabbitMQ
	docker compose stop postgres rabbitmq

## ── Tests ────────────────────────────────────────────────────────────────────
test: test-command test-query frontend-test ## Run all tests (backend + frontend)

test-command: build-shared ## Run tests for the command service
	cd $(COMMAND_SERVICE_DIR) && ./mvnw test -B

test-query: build-shared ## Run tests for the query service
	cd $(QUERY_SERVICE_DIR) && ./mvnw test -B

## ── Clean ────────────────────────────────────────────────────────────────────
clean: ## Clean all build artifacts (backend + frontend + storybook)
	cd $(SHARED_DB_DIR) && ./mvnw clean
	cd $(COMMAND_SERVICE_DIR) && ./mvnw clean
	cd $(QUERY_SERVICE_DIR) && ./mvnw clean
	rm -rf $(FRONTEND_DIR)/dist $(FRONTEND_DIR)/storybook-static

clean-docker: ## Remove containers, networks, and volumes (destructive)
	docker compose down -v --remove-orphans

## ── Storybook ───────────────────────────────────────────────────────────────
storybook: frontend-install ## Start Storybook dev server (port 6006)
	cd $(FRONTEND_DIR) && npm run storybook

storybook-build: frontend-install ## Build Storybook static site
	cd $(FRONTEND_DIR) && npm run build-storybook

storybook-docker: ## Start Storybook in Docker (port 6006)
	docker compose --profile storybook up --build -d storybook

## ── Database ─────────────────────────────────────────────────────────────────
db-reset: ## Reset both databases completely (drop all tables + Flyway history, requires running Postgres container)
	docker exec trailrace-postgres psql -U trailrace -d trailrace_command -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public; GRANT ALL ON SCHEMA public TO trailrace;"
	docker exec trailrace-postgres psql -U trailrace -d trailrace_query   -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public; GRANT ALL ON SCHEMA public TO trailrace;"
	@echo "✔ Both databases reset. Restart services to re-run Flyway migrations."






