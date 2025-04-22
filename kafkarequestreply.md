# Implementing Kafka Request/Reply Pattern between Spring Apps

## Overview
This document outlines the implementation plan for setting up a request/reply pattern using Kafka between spring-app-3 (requester) and spring-app-4 (replier) using Spring's ReplyingKafkaTemplate.

## Architecture
- **spring-app-3**: Request sender (Producer)
- **spring-app-4**: Request handler and reply sender (Consumer + Producer)
- **Topics**:
  - Request Topic: `request-topic`
  - Reply Topic: `reply-topic`

## Implementation Steps

### 1. Add Dependencies
Add the following dependency to both applications' `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
```

### 2. Configure spring-app-3 (Requester)

#### 2.1. Create Request/Reply Configuration
Create `KafkaRequestReplyConfig.java`:
```java
@Configuration
public class KafkaRequestReplyConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate(
            ProducerFactory<String, String> producerFactory,
            ConcurrentKafkaListenerContainerFactory<String, String> containerFactory) {
        
        ReplyingKafkaTemplate<String, String, String> template = 
            new ReplyingKafkaTemplate<>(producerFactory, containerFactory);
        
        template.setSharedReplyTopic(true);
        template.setDefaultReplyTimeout(Duration.ofSeconds(10));
        return template;
    }

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }
}
```

#### 2.2. Create Request Service
Create `KafkaRequestService.java`:
```java
@Service
@Slf4j
public class KafkaRequestService {
    private static final String REQUEST_TOPIC = "request-topic";
    
    @Autowired
    private ReplyingKafkaTemplate<String, String, String> replyingKafkaTemplate;
    
    public String sendRequestAndGetReply(String request) {
        try {
            ProducerRecord<String, String> record = 
                new ProducerRecord<>(REQUEST_TOPIC, request);
            
            RequestReplyFuture<String, String, String> future = 
                replyingKafkaTemplate.sendAndReceive(record);
            
            ConsumerRecord<String, String> response = future.get(10, TimeUnit.SECONDS);
            return response.value();
        } catch (Exception e) {
            log.error("Error in request/reply", e);
            throw new RuntimeException("Failed to get reply", e);
        }
    }
}
```

### 3. Configure spring-app-4 (Replier)

#### 3.1. Create Reply Configuration
Create `KafkaReplyConfig.java`:
```java
@Configuration
public class KafkaReplyConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "spring-app-4-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, String> replyTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
```

#### 3.2. Create Reply Service
Create `KafkaReplyService.java`:
```java
@Service
@Slf4j
public class KafkaReplyService {
    private static final String REQUEST_TOPIC = "request-topic";
    
    @Autowired
    private KafkaTemplate<String, String> replyTemplate;
    
    @KafkaListener(topics = REQUEST_TOPIC, groupId = "spring-app-4-group")
    public void listen(String request, @Header(KafkaHeaders.REPLY_TOPIC) String replyTopic,
                      @Header(KafkaHeaders.CORRELATION_ID) String correlationId) {
        log.info("Received request: {}", request);
        
        // Process the request and generate reply
        String reply = processRequest(request);
        
        // Send the reply
        ProducerRecord<String, String> replyRecord = 
            new ProducerRecord<>(replyTopic, null, correlationId, null, reply);
        
        replyTemplate.send(replyRecord)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Reply sent successfully");
                } else {
                    log.error("Failed to send reply", ex);
                }
            });
    }
    
    private String processRequest(String request) {
        // Add your business logic here
        return "Processed: " + request;
    }
}
```

### 4. Update Application Properties

#### 4.1. spring-app-3 application.properties
Add:
```properties
spring.kafka.producer.properties.enable.idempotence=true
spring.kafka.producer.properties.max.in.flight.requests.per.connection=1
```

#### 4.2. spring-app-4 application.properties
Add:
```properties
spring.kafka.consumer.properties.isolation.level=read_committed
spring.kafka.consumer.properties.enable.auto.commit=false
```

### 5. Create Controller in spring-app-3
Create `KafkaRequestController.java`:
```java
@RestController
@RequestMapping("/api/kafka")
@Slf4j
public class KafkaRequestController {
    @Autowired
    private KafkaRequestService requestService;
    
    @PostMapping("/request")
    public ResponseEntity<String> sendRequest(@RequestBody String request) {
        try {
            String reply = requestService.sendRequestAndGetReply(request);
            return ResponseEntity.ok(reply);
        } catch (Exception e) {
            log.error("Error processing request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error processing request: " + e.getMessage());
        }
    }
}
```

## Testing the Implementation

1. Start both applications
2. Send a test request using curl:
```bash
curl -X POST -H "Content-Type: text/plain" -d "Hello from app-3" http://localhost:8080/api/kafka/request
```

## Error Handling and Best Practices

1. **Timeout Handling**: Set appropriate timeouts for request/reply operations
2. **Retry Mechanism**: Implement retry logic for failed requests
3. **Error Topics**: Consider implementing dead letter queues for failed messages
4. **Monitoring**: Add metrics and logging for request/reply operations
5. **Security**: Implement proper authentication and authorization
6. **Message Validation**: Validate incoming requests and replies
7. **Circuit Breaker**: Implement circuit breaker pattern for fault tolerance

## Performance Considerations

1. **Connection Pooling**: Configure appropriate connection pool sizes
2. **Batch Processing**: Consider batch processing for high-volume scenarios
3. **Message Size**: Keep message sizes reasonable
4. **Timeout Values**: Set appropriate timeout values based on your use case
5. **Concurrency**: Configure appropriate concurrency levels

## Monitoring and Observability

1. Add metrics for:
   - Request/Reply latency
   - Success/Failure rates
   - Message processing times
   - Queue sizes
2. Implement distributed tracing
3. Add comprehensive logging
4. Set up alerts for error conditions

## Security Considerations

1. Implement SSL/TLS for Kafka connections
2. Use authentication for Kafka access
3. Implement proper authorization
4. Encrypt sensitive data in messages
5. Implement proper error handling to prevent information leakage
