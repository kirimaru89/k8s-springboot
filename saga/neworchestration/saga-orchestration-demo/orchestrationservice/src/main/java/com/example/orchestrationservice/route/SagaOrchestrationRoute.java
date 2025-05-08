package com.example.orchestrationservice.route;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.saga.CamelSagaService;
import org.apache.camel.saga.InMemorySagaService;
import org.apache.camel.model.SagaPropagation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

@Component
public class SagaOrchestrationRoute extends RouteBuilder {

    @Value("${services.step1.url:'http://step1service:8081'}")
    private String step1ServiceUrl;

    @Value("${services.step2.url:'http://step2service:8082'}")
    private String step2ServiceUrl;

    @Value("${services.step3.url:'http://step3service:8083'}")
    private String step3ServiceUrl;

    @Bean
    public CamelSagaService sagaService() {
        return new InMemorySagaService();
    }

    @Override
    public void configure() throws Exception {

        // Orchestration Route - one saga context
        from("direct:startSaga")
            .routeId("sagaOrchestrationRoute")
            .log("🔁 Starting saga for input: ${body}")
            .setProperty("inputPayload", body())

            // All steps participate in the same saga context
            .to("direct:sagaStep1")
            .to("direct:sagaStep2")
            .to("direct:sagaStep3")

            .log("✅ Saga completed for input: ${body}")
            .setBody(simple("Saga flow finished with: ${body}"));

        // Step 1 (joins saga and defines compensation)
        from("direct:sagaStep1")
            .routeId("sagaStep1Route")
            .saga()
                .propagation(SagaPropagation.REQUIRED)
                .compensation("direct:compensateStep1")
            .log("➡️ Executing Step 1")
            .setHeader(Exchange.HTTP_METHOD, constant(HttpMethod.POST))
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            .toD(step1ServiceUrl + "/step1/execute")
            .log("✅ Step 1 done");

        // Step 2 (joins saga and defines compensation)
        from("direct:sagaStep2")
            .routeId("sagaStep2Route")
            .saga()
                .propagation(SagaPropagation.REQUIRED)
                .compensation("direct:compensateStep2")
            .log("➡️ Executing Step 2")
            .setBody(exchangeProperty("inputPayload"))
            .setHeader(Exchange.HTTP_METHOD, constant(HttpMethod.POST))
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            .toD(step2ServiceUrl + "/step2/execute")
            .log("✅ Step 2 done");

        // Step 3 (joins saga and defines compensation)
        from("direct:sagaStep3")
            .routeId("sagaStep3Route")
            .saga()
                .propagation(SagaPropagation.REQUIRED)
                .compensation("direct:compensateStep3")
            .log("➡️ Executing Step 3")
            .setBody(exchangeProperty("inputPayload"))
            .setHeader(Exchange.HTTP_METHOD, constant(HttpMethod.POST))
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            .toD(step3ServiceUrl + "/step3/execute")
            .log("✅ Step 3 done");

        // Compensation Route for Step 1
        from("direct:compensateStep1")
            .routeId("compensateStep1Route")
            .log("🛑 Compensating Step 1")
            .setBody(exchangeProperty("inputPayload"))
            .setHeader(Exchange.HTTP_METHOD, constant(HttpMethod.POST))
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            .toD(step1ServiceUrl + "/step1/reverse")
            .log("✅ Step 1 compensated");

        // Compensation Route for Step 2
        from("direct:compensateStep2")
            .routeId("compensateStep2Route")
            .log("🛑 Compensating Step 2")
            .setBody(exchangeProperty("inputPayload"))
            .setHeader(Exchange.HTTP_METHOD, constant(HttpMethod.POST))
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            .toD(step2ServiceUrl + "/step2/reverse")
            .log("✅ Step 2 compensated");

        // Compensation Route for Step 3
        from("direct:compensateStep3")
            .routeId("compensateStep3Route")
            .log("🛑 Compensating Step 3")
            .setBody(exchangeProperty("inputPayload"))
            .setHeader(Exchange.HTTP_METHOD, constant(HttpMethod.POST))
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            .toD(step3ServiceUrl + "/step3/reverse")
            .log("✅ Step 3 compensated");
    }
}