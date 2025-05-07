package com.example.orchestrator.routes.createTransaction;

import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.SagaPropagation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.apache.camel.model.rest.RestBindingMode;

@Component
public class CreateTransactionSagaRoute extends RouteBuilder {
    @Autowired
    private ProducerTemplate producerTemplate;
    
    @Override
    public void configure() throws Exception {
        // REST Configuration
        restConfiguration()
            .component("servlet")
            .bindingMode(RestBindingMode.json);

        // REST Endpoint to start saga
        rest("/api/v1/digital-documents/library/saga")
                .post("/start-transaction")
                .to("direct:startTransactionSaga");

        // Define the saga route
        from("direct:startTransactionSaga")
                .saga()
                .propagation(SagaPropagation.REQUIRED)
                .to("direct:createTransaction")
                .to("direct:updateAccountBalance")
                .to("direct:checkFraudDetection")
                .log("Camel: Create Transaction Saga completed successfully!")
                .end();
    }
}