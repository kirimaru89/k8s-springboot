# Saga Pattern Orchestration Demo with Apache Camel

This project demonstrates the Saga pattern (Orchestration approach) using Apache Camel and Spring Boot.

## Architecture

The demo consists of four Spring Boot 3.2.5 microservices (Java 21):

-   `orchestrationservice`: The orchestrator using Apache Camel to coordinate the steps.
-   `step1service`: Performs and reverses step 1.
-   `step2service`: Performs and reverses step 2.
-   `step3service`: Performs and reverses step 3.

## Prerequisites

-   Java 21
-   Maven 3.8+
-   Docker & Docker Compose

## Building the Services

Each service is a standard Spring Boot application built with Maven.
To build an individual service (e.g., `step1service`):

```bash
cd step1service
mvn clean package
```

## Running with Docker Compose

The easiest way to run all services together is using Docker Compose.

1.  **Build and Start Services:**

    From the `saga-orchestration-demo` root directory, run:

    ```bash
docker-compose up --build
    ```

    This command will build the Docker images for each service and then start the containers.

2.  **Accessing Services:**

    -   Orchestration Service: `http://localhost:8080`
    -   Step 1 Service: `http://localhost:8081`
    -   Step 2 Service: `http://localhost:8082`
    -   Step 3 Service: `http://localhost:8083`

3.  **Triggering the Saga:**

    Send a POST request to the orchestrator's `/saga/start` endpoint with a string payload:

    ```bash
    curl -X POST -H "Content-Type: application/json" -d '"HelloSaga"' http://localhost:8080/saga/start
    ```

## Simulating Failures

The `step2service` and `step3service` can simulate failures. This is controlled by environment variables in the `docker-compose.yml` file.

-   **To make `step2service` fail on its first execution attempt:**
    Set the `SIMULATE_STEP2_FAILURE` environment variable to `true` when starting Docker Compose:
    ```bash
    SIMULATE_STEP2_FAILURE=true docker-compose up --build
    ```
    The service will then fail on odd-numbered requests to its `/execute` endpoint.

-   **To make `step3service` fail on its first execution attempt:**
    Set the `SIMULATE_STEP3_FAILURE` environment variable to `true`:
    ```bash
    SIMULATE_STEP3_FAILURE=true docker-compose up --build
    ```

When a step fails, the orchestrator will trigger the compensation actions for the preceding completed steps.

## Saga Flow

1.  **Happy Path:** Orchestrator calls `step1/execute` -> `step2/execute` -> `step3/execute`.
2.  **Failure in Step 2:** Orchestrator calls `step1/execute`. `step2/execute` fails. Orchestrator calls `step1/reverse`.
3.  **Failure in Step 3:** Orchestrator calls `step1/execute` -> `step2/execute`. `step3/execute` fails. Orchestrator calls `step2/reverse` -> `step1/reverse`.

## Actuator Endpoints

Each service exposes Spring Boot Actuator endpoints. For example:
-   `http://localhost:8080/actuator/health`
-   `http://localhost:8081/actuator/health`
-   `http://localhost:8080/actuator/prometheus` (Prometheus metrics for orchestrator)
-   `http://localhost:8080/actuator/camelroutes` (Camel routes details for orchestrator)

## Logging

-   All services use `logbook-spring-boot-starter` for HTTP request/response logging.
-   The orchestrator logs each step and compensation action clearly.

## Clean Architecture

Each service attempts to follow a clean architecture with `controller`, `service` (implicit in Camel routes/beans), and `config` layers. 