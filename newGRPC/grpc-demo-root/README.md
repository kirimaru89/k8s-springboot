# gRPC Demo Project

This project demonstrates a simple gRPC setup with two Spring Boot microservices.

- **Service A**: An HTTP service that calls Service B via gRPC.
- **Service B**: A gRPC service.

## Prerequisites

- Java 21
- Maven
- Docker
- Docker Compose

## Project Structure

```
grpc-demo-root/
├── proto/hello.proto         # Shared .proto definition
├── service-a/                # HTTP server with gRPC client
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
├── service-b/                # gRPC server
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
├── docker-compose.yml
└── README.md
```

## How to Build and Run

1.  **Build the services using Maven:**

    Navigate to the `grpc-demo-root` directory.

    Build Service A:
    ```bash
    cd service-a
    mvn clean package
    cd ..
    ```

    Build Service B:
    ```bash
    cd service-b
    mvn clean package
    cd ..
    ```

2.  **Run using Docker Compose:**

    Ensure you are in the `grpc-demo-root` directory where `docker-compose.yml` is located.

    ```bash
    docker compose up --build
    ```

    This command will build the Docker images (if not already built or if changes are detected) and start both services.

## Testing the Endpoint

Once the services are running, you can test Service A's endpoint:

Open your browser or use a tool like `curl`:

```bash
curl http://localhost:8080/hello/YourName
```

This should return:

```
Hello YourName
```

(If Service B successfully processed the gRPC call, the actual message might be like "Hello YourName" or similar, depending on Service B's logic which is to prepend "Hello ")

## Health Checks

- Service A Health: `http://localhost:8080/actuator/health`
- Service B Health (via gRPC, actual HTTP endpoint depends on Actuator config if exposed over HTTP for gRPC service):
  The gRPC health check is typically handled by the `grpc-health-probe` or similar tools. Spring Boot Actuator for gRPC services might require additional configuration to expose HTTP health endpoints if not using a gRPC health check service directly.
  For this project, Service B's health can be inferred from its logs and successful responses to Service A.

## Stopping the Services

To stop the services run via Docker Compose:

```bash
docker compose down
``` 