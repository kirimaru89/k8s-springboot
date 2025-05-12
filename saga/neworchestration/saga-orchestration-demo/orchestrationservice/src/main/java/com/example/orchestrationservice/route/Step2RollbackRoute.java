package com.example.orchestrationservice.route;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.apache.camel.model.SagaCompletionMode;
import org.apache.camel.model.SagaPropagation;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.stereotype.Component;
import org.apache.camel.model.dataformat.JsonLibrary;

@Component
public class Step2RollbackRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        from("direct:rollbackStep2")
            .saga()
            .process(exchange -> {
                String body = exchange.getProperty("originalRequestBody", String.class);
                log.info("--------------- Before rollback step2! Body: {}", body);
                exchange.setProperty("originalRequestBody", body);

                exchange.getIn().setBody(body);
            })
            .log("Before rollback step2!")
            .marshal().json()
            .to("http://step2service:8082/step2/reverse")
            .process(exchange -> {
                String body = exchange.getIn().getBody(String.class);
                log.info("--------------- After rollback step2! Body: {}", body);

                throw new RuntimeException("Business process failed, rollback completed");
            })
            .log("Rollback step2 completed successfully!")
            .end();
    }
}