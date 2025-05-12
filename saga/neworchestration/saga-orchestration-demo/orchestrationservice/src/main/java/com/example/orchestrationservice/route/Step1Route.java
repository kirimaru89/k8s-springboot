package com.example.orchestrationservice.route;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.apache.camel.model.SagaCompletionMode;
import org.apache.camel.model.SagaPropagation;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.stereotype.Component;
import org.apache.camel.model.dataformat.JsonLibrary;

@Component
public class Step1Route extends RouteBuilder {
    @Override
    public void configure() throws Exception {
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
            })
            .log("Step 1 completed successfully!")
            .end();
    }
}