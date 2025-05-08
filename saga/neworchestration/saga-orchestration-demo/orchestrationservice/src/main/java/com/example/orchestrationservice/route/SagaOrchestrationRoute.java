package com.example.orchestrationservice.route;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.SagaPropagation;
import org.apache.camel.saga.CamelSagaService;
import org.apache.camel.saga.InMemorySagaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

@Component
public class SagaOrchestrationRoute extends RouteBuilder {

    private static final Logger log = LoggerFactory.getLogger(SagaOrchestrationRoute.class);

    @Value("${services.step1.url}")
    private String step1ServiceUrl;

    @Value("${services.step2.url}")
    private String step2ServiceUrl;

    @Value("${services.step3.url}")
    private String step3ServiceUrl;

    @Bean
    public CamelSagaService sagaService() {
        return new InMemorySagaService();
    }

    @Override
    public void configure() throws Exception {
        from("direct:startSaga")
            .routeId("sagaOrchestrationRoute")
            .saga().propagation(SagaPropagation.REQUIRED) 
                .timeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .compensation("direct:compensateFullSaga") 
                .log("Starting saga for input: ${body}")

                // Step 1
                .setHeader(Exchange.HTTP_METHOD, constant(HttpMethod.POST))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .setProperty("inputPayload", body()) 
                .log("Executing Step 1 for input: ${body}")
                .toD(step1ServiceUrl + "/step1/execute")
                .setProperty("step1Completed", constant(true)) 
                .log("Step 1 executed. Response: ${body}")

                // Step 2
                .setBody(exchangeProperty("inputPayload")) 
                .setHeader(Exchange.HTTP_METHOD, constant(HttpMethod.POST))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .log("Executing Step 2 for input: ${body}")
                .toD(step2ServiceUrl + "/step2/execute")
                .setProperty("step2Completed", constant(true)) 
                .log("Step 2 executed. Response: ${body}")

                // Step 3
                .setBody(exchangeProperty("inputPayload")) 
                .setHeader(Exchange.HTTP_METHOD, constant(HttpMethod.POST))
                .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                .log("Executing Step 3 for input: ${body}")
                .toD(step3ServiceUrl + "/step3/execute")
                .setProperty("step3Completed", constant(true)) 
                .log("Step 3 executed. Response: ${body}")

            .end() 
            .log("Saga completed successfully for input: ${body}")
            .setBody(simple("Saga flow finished with: ${body}"));

        // Single Compensation Route
        from("direct:compensateFullSaga")
            .routeId("compensateFullSagaRoute")
            .log("Starting full saga compensation. Input: ${body}")
            .choice()
                .when(exchangeProperty("step3Completed").isEqualTo(true))
                    .log("Reversing Step 3...")
                    .setBody(exchangeProperty("inputPayload"))
                    .setHeader(Exchange.HTTP_METHOD, constant(HttpMethod.POST))
                    .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                    .toD(step3ServiceUrl + "/step3/reverse")
                    .log("Step 3 reversed. Response: ${body}")
                .end() 
            .choice()
                .when(exchangeProperty("step2Completed").isEqualTo(true))
                    .log("Reversing Step 2...")
                    .setBody(exchangeProperty("inputPayload"))
                    .setHeader(Exchange.HTTP_METHOD, constant(HttpMethod.POST))
                    .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                    .toD(step2ServiceUrl + "/step2/reverse")
                    .log("Step 2 reversed. Response: ${body}")
                .end() 
             .choice()
                .when(exchangeProperty("step1Completed").isEqualTo(true))
                    .log("Reversing Step 1...")
                    .setBody(exchangeProperty("inputPayload"))
                    .setHeader(Exchange.HTTP_METHOD, constant(HttpMethod.POST))
                    .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
                    .toD(step1ServiceUrl + "/step1/reverse")
                    .log("Step 1 reversed. Response: ${body}")
                .end() 
            .log("Full saga compensation finished.");
    }
} 