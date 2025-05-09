package com.example.orchestrationservice.route;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.SagaCompletionMode;
import org.apache.camel.model.SagaPropagation;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.stereotype.Component;

@Component
public class SagaRouteBuilder extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        // REST Configuration
        restConfiguration()
            .component("servlet")
            .bindingMode(RestBindingMode.json);

        // REST Endpoint to start saga
        rest("/saga")
            .post("/start")
            .to("direct:startSagaOrchestration");

        // Define the saga route
        from("direct:startSagaOrchestration")
            .saga()
                .propagation(SagaPropagation.REQUIRED)
            .completionMode(SagaCompletionMode.AUTO)
            .to("direct:step1")
            .to("direct:step2")
            // .to("direct:step3")
            .log("Saga completed successfully!")
            .end();
        
        // from("direct:createTransaction")
        //     .saga()
        //     // 1. Lưu dữ liệu gốc vào property
        //     .process(exchange -> {
        //         CreateTransactionRequestDto originalRequest = exchange.getIn().getBody(CreateTransactionRequestDto.class);
        //         exchange.setProperty("originalRequestBody", originalRequest);
        //     })
        //     .log("Camel: [TransactionService] Creating transaction with ID")
        //     .marshal().json()   // <-- Serialize to JSON
        //     .to(transactionServiceUri)
        //     .process(exchange -> {
        //         /* cần unwrap ResponseEntity */
        //         // Lấy response body
        //         String body = exchange.getIn().getBody(String.class);
        //         ApiResponseDto<TransactionResponseDto> parsed = JsonUtils.readValue(body, new TypeReference<ApiResponseDto<TransactionResponseDto>>() {});
        //         exchange.getIn().setBody(parsed);

        //         /* cần bổ sung transactionId */
        //         exchange.setProperty("transactionId", parsed.getData().getId());
        //     })
        //     .log("Camel: [TransactionService] Transaction created successfully")
        //     .end();

        from("direct:step1")
            .saga()
            .process(exchange -> {
            })
            .log("Before execute step1!")
            .marshal().json()
            .to("http://step1service:8081/step1/execute")
            .process(exchange -> {
                String body = exchange.getIn().getBody(String.class);
                log.info("Step 1! Body: {}", body);
            })
            .log("Step 1 completed successfully!")
            .end();

        from("direct:step2")
            .saga()
            .process(exchange -> {
            })
            .log("Before execute step2!")
            .marshal().json()
            .to("http://step2service:8082/step2/execute")
            .process(exchange -> {
                String body = exchange.getIn().getBody(String.class);
                log.info("Step 2! Body: {}", body);
            })
            .log("Step 2 completed successfully!")
            .end();

        // from("direct:step2")
        //     .log("Step 2 completed successfully!")
        //     .end();

        // from("direct:step3")
        //     .log("Step 3 completed successfully!")
        //     .end();
    }
}