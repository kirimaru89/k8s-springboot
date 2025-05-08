Generate a complete Java demo project to demonstrate the **Saga Pattern (Choreography approach)** with the following specifications:

## 🎯 Purpose:
Simulate a distributed transaction across 3 microservices using Kafka as the message broker and the **choreography-based Saga pattern** for orchestration.

## 🧱 Architecture:
- 3 microservices (all Spring Boot 3.2.5 apps with Java 21 and HTTP endpoints):
  - **step1service**
  - **step2service**
  - **step3service**
- Each service should expose a REST endpoint to perform a step and another to compensate (reverse) it.

## 🔁 Transaction Flow:
1. **Happy case**:  
   - step1 → step2 → step3  
   - Each step is triggered by Kafka messages
2. **Failed in step2**:  
   - Reverse step2 → reverse step1
3. **Failed in step3**:  
   - Reverse step3 → reverse step2 → reverse step1

Each step only accepts a string payload and returns a string result.

## 🔧 Technical Requirements:
- Java 21
- Spring Boot 3.2.5
- Kafka as message broker (use Spring Kafka)
- Use `WebClient` for HTTP calls (e.g., during compensation if needed)
- Use `logbook-spring-boot-starter` in each service to log all incoming and outgoing HTTP requests/responses
- Log the Kafka messages sent/received
- Use Docker to containerize each service
- Use `docker-compose.yml` to spin up all services and Kafka broker together
- Proper exception handling and retries
- Follow clean code and Spring Boot best practices

## 📦 Project Structure:
- `saga-choreography-demo/`
  - `step1service/`
  - `step2service/`
  - `step3service/`
  - `docker-compose.yml`
  - `README.md`

## ✍️ Expected Components in Each Service:
- Kafka config (consumer + producer)
- Controller (to start local processing)
- Event handlers (listening to relevant topics)
- Compensation logic
- WebClient config
- Logbook config in `application.yml`

## 🧪 Bonus:
- Add actuator endpoints (`/actuator/health`) for health checks
- Include log messages clearly showing the saga flow
- Each reverse step should log a statement like "Reversing step2 for transaction {id}"

Generate all source code, configs, and docker setup.