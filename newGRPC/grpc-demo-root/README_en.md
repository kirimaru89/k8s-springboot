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

## gRPC Workflow Details

This section explains how gRPC is used for communication between Service A and Service B.

### 1. Protocol Definition (`proto/hello.proto`)

The communication contract between the gRPC client (in Service A) and the gRPC server (in Service B) is defined in the `proto/hello.proto` file using Protocol Buffers.

Key elements in `hello.proto`:
- **`service Greeter`**: Defines a service named `Greeter`.
- **`rpc SayHello (StringRequest) returns (HelloResponse) {}`**: Specifies a remote procedure call (RPC) method named `SayHello`. This method takes a `StringRequest` message as input and returns a `HelloResponse` message.
- **`message StringRequest`**: Defines the structure of the request message, containing a single string field `name`.
- **`message HelloResponse`**: Defines the structure of the response message, containing a single string field `message`.
- **`option java_package = "com.example.grpc";`**: Specifies the Java package where the generated Java code will reside.
- **`option java_multiple_files = true;`**: Instructs the generator to create separate Java files for each message and service, rather than a single outer class containing everything.

### 2. Code Generation (Maven Build)

When you build `service-a` and `service-b` using Maven (`mvn clean package`), the `protobuf-maven-plugin` (configured in their respective `pom.xml` files) processes `proto/hello.proto`.

- The plugin is configured with `<protoSourceRoot>../proto</protoSourceRoot>`. This tells Maven to look for the `.proto` files in a directory named `proto` located one level above the current service's directory (i.e., in `grpc-demo-root/proto/`). This allows both services to share the same `hello.proto` definition.

This plugin performs two main tasks:
1.  **Compiles `.proto` to Java**: It uses `protoc` (the Protocol Buffer compiler) to generate Java classes from the `.proto` definition. These classes include:
    *   `StringRequest.java` and `HelloResponse.java`: For creating and manipulating request and response messages.
    *   `GreeterGrpc.java`: Contains the client stub and server base implementation for the `Greeter` service.
        *   **Client Stub (`GreeterGrpc.GreeterBlockingStub` or `GreeterGrpc.GreeterFutureStub`)**: Used by Service A to make calls to the `SayHello` method.
        *   **Server Base Class (`GreeterGrpc.GreeterImplBase`)**: Extended by Service B to implement the actual logic for the `SayHello` method.

These generated Java files are placed in the `target/generated-sources/protobuf/java` and `target/generated-sources/protobuf/grpc-java` directories of each service and are automatically included in the compilation classpath.

### 3. Service B (gRPC Server Implementation)

-   **`service-b/src/main/java/com/example/serviceb/service/GrpcServerService.java`**:
    *   This class extends `GreeterGrpc.GreeterImplBase` (the generated base class).
    *   It overrides the `sayHello` method to provide the actual implementation. When called, it constructs a `HelloResponse` and sends it back to the client.
-   **gRPC Server Startup**: The `net.devh:grpc-server-spring-boot-starter` dependency in `service-b` automatically discovers beans annotated with `@GrpcService` (like `GrpcServerService`) and starts a gRPC server listening on the configured port (e.g., 9090).

### 4. Service A (gRPC Client Implementation)

-   **`service-a/src/main/java/com/example/servicea/config/GrpcClientConfig.java`**:
    *   This configuration class creates a `ManagedChannel`. A `ManagedChannel` represents a connection to a gRPC server.
    *   It's configured to connect to Service B (address `service-b`, port `9090` - overridden by Docker Compose for inter-container communication).
    *   It also registers interceptors like `LoggingClientInterceptor` (for custom logging) and `ObservationGrpcClientInterceptor` (for distributed tracing).
-   **`service-a/src/main/java/com/example/servicea/client/GrpcClient.java`**:
    *   This client component receives the `ManagedChannel` as a dependency.
    *   It creates a gRPC client stub using `GreeterGrpc.newBlockingStub(channel)`.
    *   The `sayHello(String name)` method in this client uses the stub to:
        1.  Create a `StringRequest`.
        2.  Call the `sayHello` RPC method on the stub, sending the request.
        3.  Receive the `HelloResponse` from Service B.
-   **Integration**: `HelloController` -> `GreetingService` -> `GrpcClient` is the flow within Service A to trigger the gRPC call.

### Summary of Interaction

1.  A user sends an HTTP GET request to `/hello/{name}` on Service A.
2.  Service A's `HelloController` calls `GreetingService`.
3.  `GreetingService` calls `GrpcClient`.
4.  `GrpcClient` uses the generated `GreeterGrpc.GreeterBlockingStub` (configured with a `ManagedChannel` pointing to Service B) to invoke the `sayHello` RPC method.
    *   The client stub serializes the `StringRequest` into the Protocol Buffer binary format.
    *   Trace context (if configured, like with Micrometer) is injected into the gRPC metadata (headers).
5.  The request travels over the network to Service B's gRPC server.
6.  Service B's `GrpcServerService` (the `GreeterImplBase` implementation) receives the request.
    *   Trace context is extracted from metadata.
    *   The server deserializes the `StringRequest`.
7.  `GrpcServerService` processes the request and creates a `HelloResponse`.
8.  The server serializes the `HelloResponse` and sends it back.
9.  Service A's `GrpcClient` receives the `HelloResponse`, deserializes it, and returns the message content.
10. Service A sends the message back as the HTTP response.

This setup allows for efficient, strongly-typed communication between the microservices, leveraging Protocol Buffers for serialization and gRPC for the RPC mechanism.

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