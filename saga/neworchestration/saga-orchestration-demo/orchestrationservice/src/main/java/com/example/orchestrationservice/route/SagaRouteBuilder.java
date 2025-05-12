package com.example.orchestrationservice.route;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.apache.camel.model.SagaCompletionMode;
import org.apache.camel.model.SagaPropagation;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.stereotype.Component;
import org.apache.camel.model.dataformat.JsonLibrary;

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
            .log("Saga completed successfully!")
            .end();
    }
}