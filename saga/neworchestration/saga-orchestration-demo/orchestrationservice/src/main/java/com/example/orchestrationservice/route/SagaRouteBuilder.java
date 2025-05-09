package com.example.orchestrationservice.route;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.http.base.HttpOperationFailedException;
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
                // .compensation("direct:rollbackStep2")
            .to("direct:step3")
                // .compensation("direct:rollbackStep3")
            .log("Saga completed successfully!")
            .end();

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
                String body = exchange.getIn().getBody(String.class);
                log.info("Step 2! Body: {}", body);
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

        from("direct:rollbackStep2")
            .log("Rollback step2!")
            .end();

        from("direct:step3")
            .saga()
            .process(exchange -> {
                String body = exchange.getIn().getBody(String.class);
                log.info("Step 3! Body: {}", body);
            })
            .log("Before execute step3!")
            .marshal().json()
            .to("http://step3service:8083/step3/execute")
            .process(exchange -> {
                String body = exchange.getIn().getBody(String.class);
                log.info("Step 3! Body: {}", body);
            })
            .log("Step 3 completed successfully!")
            .end();

        from("direct:rollbackStep3")
            .log("Rollback step3!")
            .end();
    }
}