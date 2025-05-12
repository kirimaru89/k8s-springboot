package com.example.orchestrationservice.route;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.apache.camel.model.SagaCompletionMode;
import org.apache.camel.model.SagaPropagation;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.stereotype.Component;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.Exchange;

@Component
public class SagaRouteBuilder extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        // REST Configuration
        restConfiguration()
            .component("servlet")
            .bindingMode(RestBindingMode.json);

        // Global exception handling
        onException(HttpOperationFailedException.class)
            .handled(true)
            .process(exchange -> {
                String step = exchange.getProperty("step", String.class);
                log.info("Error in step: {}", step);
            })
            .choice()
                .when(exchangeProperty("step").isEqualTo("step2"))
                    .to("direct:rollbackStep1")
                    .to("direct:handleError")
                .when(exchangeProperty("step").isEqualTo("step3"))
                    .to("direct:rollbackStep2")
                    .to("direct:rollbackStep1")
                    .to("direct:handleError")
            .end();
        
        from("direct:handleError")
            .process(exchange -> {
                String step = exchange.getProperty("step", String.class);
                
                exchange.getIn().setHeader("CamelHttpResponseCode", 400);
                exchange.getIn().setBody("Error in " + step + ": " + exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class).getMessage());
            })
            .stop();

        // REST Endpoint
        rest("/saga")
            .post("/start")
            .to("direct:startSagaOrchestration");

        // Main saga route
        from("direct:startSagaOrchestration")
            .saga()
                .propagation(SagaPropagation.REQUIRED)
            .completionMode(SagaCompletionMode.AUTO)
            .to("direct:step1")
            .to("direct:step2")
            .to("direct:step3")
            .log("Saga completed successfully!")
            .end();

        // Step routes
        from("direct:step1")
            .saga()
            .process(exchange -> {
                String body = exchange.getIn().getBody(String.class);
                exchange.setProperty("originalRequestBody", body);
                exchange.setProperty("step", "step1");
            })
            .marshal().json(JsonLibrary.Jackson)
            .to("http://step1service:8081/step1/execute")
            .convertBodyTo(String.class)
            .end();

        from("direct:step2")
            .saga()
            .process(exchange -> {
                String body = exchange.getProperty("originalRequestBody", String.class);
                exchange.getIn().setBody(body);
                exchange.setProperty("step", "step2");
            })
            .marshal().json(JsonLibrary.Jackson)
            .to("http://step2service:8082/step2/execute")
            .convertBodyTo(String.class)
            .end();

        from("direct:step3")
            .saga()
            .process(exchange -> {
                String body = exchange.getProperty("originalRequestBody", String.class);
                exchange.getIn().setBody(body);
                exchange.setProperty("step", "step3");
            })
            .marshal().json(JsonLibrary.Jackson)
            .to("http://step3service:8083/step3/execute")
            .convertBodyTo(String.class)
            .end();

        // Rollback routes
        from("direct:rollbackStep1")
            .saga()
            .process(exchange -> {
                String body = exchange.getProperty("originalRequestBody", String.class);
                exchange.getIn().setBody(body);
            })
            .marshal().json(JsonLibrary.Jackson)
            .to("http://step1service:8081/step1/reverse")
            .convertBodyTo(String.class)
            .end();

        from("direct:rollbackStep2")
            .saga()
            .process(exchange -> {
                String body = exchange.getProperty("originalRequestBody", String.class);
                exchange.getIn().setBody(body);
            })
            .marshal().json(JsonLibrary.Jackson)
            .to("http://step2service:8082/step2/reverse")
            .convertBodyTo(String.class)
            .end();
    }
}