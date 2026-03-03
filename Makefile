# Trail Race Application — Makefile
# ─────────────────────────────────────────────────────────────────────────────
# Prerequisites: Docker, Docker Compose, JDK 21, Maven 3.9+, Node.js 18+
# ─────────────────────────────────────────────────────────────────────────────

COMMAND_SERVICE_DIR = backend/race_application_command_service
QUERY_SERVICE_DIR   = backend/race_application_query_service
FRONTEND_DIR        = frontend

# Force TLS 1.2 for Maven downloads (works around TLS interception by some
# corporate proxies / antivirus tools that cause "bad_record_mac" errors)
export MAVEN_OPTS ?= -Dhttps.protocols=TLSv1.2 -Djdk.tls.client.protocols=TLSv1.2

.PHONY: help build build-command build-query build-frontend \
        frontend-install frontend-dev frontend-test \
        up down restart logs logs-command logs-query logs-frontend \
        test test-command test-query \
        infra-up infra-down clean

## ── Help ─────────────────────────────────────────────────────────────────────
help: ## Show this help message
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | \
	  awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-25s\033[0m %s\n", $$1, $$2}'

## ── Build ────────────────────────────────────────────────────────────────────
build: build-command build-query build-frontend ## Build all services (backend + frontend)

build-command: ## Build the command service JAR
	cd $(COMMAND_SERVICE_DIR) && ./mvnw clean package -DskipTests -B

build-query: ## Build the query service JAR
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

test-command: ## Run tests for the command service
	cd $(COMMAND_SERVICE_DIR) && ./mvnw test -B

test-query: ## Run tests for the query service
	cd $(QUERY_SERVICE_DIR) && ./mvnw test -B

## ── Clean ────────────────────────────────────────────────────────────────────
clean: ## Clean all build artifacts (backend + frontend)
	cd $(COMMAND_SERVICE_DIR) && ./mvnw clean
	cd $(QUERY_SERVICE_DIR) && ./mvnw clean
	rm -rf $(FRONTEND_DIR)/dist

clean-docker: ## Remove containers, networks, and volumes (destructive)
	docker compose down -v --remove-orphans






