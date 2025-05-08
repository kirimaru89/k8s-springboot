# Saga Choreography Demo

This project demonstrates the Saga pattern (Choreography approach) using Spring Boot and Kafka.

## Architecture
- **step1service**: Initiates the transaction.
- **step2service**: Second step in the transaction.
- **step3service**: Final step in the transaction.
- **Kafka**: Message broker for communication between services.

## Prerequisites
- Docker
- Docker Compose
- Java 21
- Maven

## How to Run
1. Clone the repository.
2. Navigate to the `saga-choreography-demo` directory.
3. Run the services using Docker Compose:
   ```bash
   docker-compose up --build
   ```

## Endpoints
Each service exposes the following endpoints:
- `POST /api/step` (or `/api/step1`, `/api/step2`, `/api/step3` respectively for a more direct trigger if needed for testing, though primary flow is via Kafka)
- `POST /api/compensate` (or `/api/compensate1`, etc.)

Actual transaction initiation would typically be via `step1service`.

### Health Checks
- `step1service`: `http://localhost:8081/actuator/health`
- `step2service`: `http://localhost:8082/actuator/health`
- `step3service`: `http://localhost:8083/actuator/health`

## Transaction Flow
1. **Happy Path**: `step1service` -> `step2service` -> `step3service` (via Kafka messages)
2. **Failure Scenarios**: Compensating transactions are triggered if any step fails.

## Technical Details
- Spring Boot 3.2.5
- Java 21
- Spring Kafka
- Logbook for HTTP logging
- WebClient for internal HTTP calls (primarily for compensations if direct calls are chosen over Kafka for some compensation signals). 