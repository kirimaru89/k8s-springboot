Generate a complete gRPC demo project with the following structure and technologies:

## Architecture:
- 2 microservices:
  - **Service A (HTTP)**: Spring Boot 3.2.5 app that exposes a REST endpoint `/hello/{name}` and calls Service B using gRPC.
  - **Service B (gRPC)**: Spring Boot 3.2.5 app that exposes a gRPC method `SayHello(StringRequest) -> HelloResponse` returning "Hello {name}".

## Technologies:
- Java 21
- Spring Boot 3.2.5
- Maven as the build system
- Docker for containerizing both services
- Docker Compose to run both together

## Project Structure:
- `grpc-demo-root/`
  - `proto/hello.proto` (shared .proto definition)
  - `service-a/` (HTTP server with gRPC client)
  - `service-b/` (gRPC server)
  - `docker-compose.yml`
  - `README.md`

## Requirements:
- Service A exposes `/hello/{name}` endpoint (HTTP GET)
- Internally calls Service B's `SayHello` gRPC method and returns the result
- Service B handles `StringRequest { string name = 1; }` and returns `HelloResponse { string message = 1; }`
- Proper separation of concerns (`controller`, `service`, `client`, `config`)
- YAML config (`application.yml`)
- Health check endpoint using Spring Boot Actuator
- Dockerfiles for both services
- docker-compose with working service-to-service communication
- Logging of request and response in Service A
- Follow clean code and Spring Boot best practices

## Bonus (optional):
- Add a simple unit test for Service A that mocks the gRPC client
- Add README instructions to build and run the project

Generate all necessary files and structure.