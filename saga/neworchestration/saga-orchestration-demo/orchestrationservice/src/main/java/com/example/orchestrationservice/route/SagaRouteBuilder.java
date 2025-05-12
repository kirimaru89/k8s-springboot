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

        onException(HttpOperationFailedException.class)
            .handled(true)
            .process(exchange -> {
                String step = exchange.getProperty("step", String.class);
                log.info("--------------- OnException: Step: {}", step);
            })
            .choice()
                .when(exchangeProperty("step").isEqualTo("step2"))
                    .to("direct:rollbackStep1")
                    .to("direct:handleError400")
                .when(exchangeProperty("step").isEqualTo("step3"))
                    .to("direct:rollbackStep2")
                    .to("direct:rollbackStep1")
                    .to("direct:handleError500")
                .otherwise()
                    .to("direct:rollbackStep1")
                    .to("direct:handleError500")
            .end();

        // Error handling routes
        from("direct:handleError400")
            .process(exchange -> {
                exchange.getIn().setHeader("CamelHttpResponseCode", 400);
                exchange.getIn().setBody("Error in step2: " + exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class).getMessage());
            })
            .stop();

        from("direct:handleError500")
            .process(exchange -> {
                exchange.getIn().setHeader("CamelHttpResponseCode", 500);
                exchange.getIn().setBody("Error in step3: " + exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class).getMessage());
            })
            .stop();

        // REST Endpoint to start saga
        rest("/saga")
            .post("/start")
            .responseMessage().code(200).endResponseMessage()
            .responseMessage().code(400).endResponseMessage()
            .responseMessage().code(500).endResponseMessage()
            .to("direct:startSagaOrchestration");

        // Define the saga route
        from("direct:startSagaOrchestration")
            .saga()
                .propagation(SagaPropagation.REQUIRED)
            .completionMode(SagaCompletionMode.AUTO)
            .to("direct:step1")
            .to("direct:step2")
            .log("Saga completed successfully!")
            .end();

        from("direct:step1")
            .saga()
            .process(exchange -> {
                String body = exchange.getIn().getBody(String.class);
                log.info("--------------- Before execute step1! Body: {}", body);
                exchange.setProperty("originalRequestBody", body);
            })
            .log("Before execute step1!")
            .marshal().json()
            .to("http://step1service:8081/step1/execute")
            .process(exchange -> {
                String body = exchange.getIn().getBody(String.class);
                log.info("--------------- After execute step1! Body: {}", body);

                // set step to step2
                exchange.setProperty("step", "step2");
            })
            .log("Step 1 completed successfully!")
            .end();

        from("direct:rollbackStep1")
            .saga()
            .process(exchange -> {
                String body = exchange.getProperty("originalRequestBody", String.class);
                log.info("--------------- Before rollback step1! Body: {}", body);
                exchange.setProperty("originalRequestBody", body);

                exchange.getIn().setBody(body);
            })
            .log("Before rollback step1!")
            .marshal().json(JsonLibrary.Jackson)
            .to("http://step1service:8081/step1/reverse")
            .convertBodyTo(String.class)
            .process(exchange -> {
                String body = exchange.getIn().getBody(String.class);
                log.info("--------------- After rollback step1! Body: {}", body);
            })
            .log("Rollback step1 completed successfully!")
            .end();

        from("direct:step2")
            .saga()
            .process(exchange -> {
                // print step exchange property
                log.info("Step 2: --------------- Step: {}", exchange.getProperty("step", String.class));

                String body = exchange.getProperty("originalRequestBody", String.class);
                log.info("--------------- Before execute step2! Body: {}", body);
                exchange.getIn().setBody(body);
            })
            .log("Before execute step2!")
            .marshal().json(JsonLibrary.Jackson)
            .to("http://step2service:8082/step2/execute")
            .unmarshal().json(JsonLibrary.Jackson)
            .process(exchange -> {
                String body = exchange.getIn().getBody(String.class);
                log.info("--------------- After execute step2! Body: {}", body);
            })
            .log("Step 2 completed successfully!")
            .end();

        from("direct:rollbackStep2")
            .saga()
            .process(exchange -> {
                String body = exchange.getProperty("originalRequestBody", String.class);
                log.info("--------------- Before rollback step2! Body: {}", body);
                exchange.setProperty("originalRequestBody", body);

                exchange.getIn().setBody(body);
            })
            .log("Before rollback step2!")
            .marshal().json(JsonLibrary.Jackson)
            .to("http://step2service:8082/step2/reverse")
            .unmarshal().json(JsonLibrary.Jackson)
            .process(exchange -> {
                String body = exchange.getIn().getBody(String.class);
                log.info("--------------- After rollback step2! Body: {}", body);
            })
            .log("Rollback step2 completed successfully!")
            .end();

        from("direct:step3")
            .saga()
            .process(exchange -> {
                // print step exchange property
                log.info("Step 3: --------------- Step: {}", exchange.getProperty("step", String.class));

                String body = exchange.getProperty("originalRequestBody", String.class);
                log.info("--------------- Before execute step3! Body: {}", body);
                exchange.getIn().setBody(body);
            })
            .log("Before execute step3!")
            .marshal().json(JsonLibrary.Jackson)
            .to("http://step3service:8083/step3/execute")
            .unmarshal().json(JsonLibrary.Jackson)
            .process(exchange -> {
                String body = exchange.getIn().getBody(String.class);
                log.info("--------------- After execute step3! Body: {}", body);
            })
            .log("Step 3 completed successfully!")
            .end();
    }
}