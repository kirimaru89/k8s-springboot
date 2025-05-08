Generate a complete Java demo project to demonstrate the **Saga Pattern (Orchestration approach)** using **Apache Camel** with the following specifications:

## 🧱 Architecture:
- 4 Spring Boot 3.2.5 microservices (Java 21):
  - `orchestrationservice`: the orchestrator using Apache Camel to coordinate the steps
  - `step1service`: performs and reverses step 1
  - `step2service`: performs and reverses step 2
  - `step3service`: performs and reverses step 3

## 🔁 Saga Transaction Flow:
1. **Happy path**:  
   - Orchestrator calls `step1` → `step2` → `step3` via HTTP using Apache Camel routes
2. **Failure in step2**:  
   - Call `step2/reverse` → then `step1/reverse`
3. **Failure in step3**:  
   - Call `step3/reverse` → `step2/reverse` → `step1/reverse`

## 🔧 Functional Requirements:
- Each step service:
  - Exposes `/execute` and `/reverse` endpoints
  - Receives and returns a string in both
- `orchestrationservice`:
  - Uses Apache Camel and Camel Saga DSL (e.g., `saga().compensation(...)`)
  - Triggers the saga via an HTTP endpoint `/start`

## ⚙️ Technical Requirements:
- Java 21
- Spring Boot 3.2.5
- Apache Camel 4.x with Spring Boot
- Use `WebClient` for HTTP requests
- Use `logbook-spring-boot-starter` in all services to log request/response
- Containerize each service with Docker
- Use `docker-compose.yml` to orchestrate all services together
- Use clean architecture: controller, service, config layers
- Use `application.yml` for configuration
- Use standard error handling and logging in Camel routes

## 📦 Project Structure:
- `saga-orchestration-demo/`
  - `orchestrationservice/`
  - `step1service/`
  - `step2service/`
  - `step3service/`
  - `docker-compose.yml`
  - `README.md`

## 🧪 Bonus:
- Add Spring Boot Actuator to all services for `/actuator/health`
- Log every step and reverse step from orchestrator with clear message:  
  e.g., `Executing step2 for input: abc`, `Reversing step1 for input: abc`

Generate the full source code, configuration, and Docker setup.