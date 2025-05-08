package com.example.orchestrationservice.route;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.saga.CamelSagaService;
import org.apache.camel.saga.InMemorySagaService;
import org.apache.camel.model.SagaPropagation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

@Component
public class SagaOrchestrationRoute extends RouteBuilder {

    private static final Logger log = LoggerFactory.getLogger(SagaOrchestrationRoute.class);

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
        // Entry route that invokes each step sequentially
        from("direct:startSaga")
            .routeId("sagaOrchestrationRoute")
            .log("🔁 Starting full orchestration saga with input: ${body}")
            .setProperty("inputPayload", body())
            .to("direct:sagaStep1")
            .to("direct:sagaStep2")
            .to("direct:sagaStep3")
            .log("🎉 Saga completed successfully for input: ${body}")
            .setBody(simple("Saga flow finished with: ${body}"));

        // Step 1 - With its own saga context
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

        // Step 2 - With its own saga context
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

        // Step 3 - With its own saga context
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

        // Compensation routes remain unchanged
        from("direct:compensateStep1")
            .routeId("compensateStep1Route")
            .log("🔁 Reversing Step 1")
            .setBody(exchangeProperty("inputPayload"))
            .setHeader(Exchange.HTTP_METHOD, constant(HttpMethod.POST))
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            .toD(step1ServiceUrl + "/step1/reverse");

        from("direct:compensateStep2")
            .routeId("compensateStep2Route")
            .log("🔁 Reversing Step 2")
            .setBody(exchangeProperty("inputPayload"))
            .setHeader(Exchange.HTTP_METHOD, constant(HttpMethod.POST))
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            .toD(step2ServiceUrl + "/step2/reverse");

        from("direct:compensateStep3")
            .routeId("compensateStep3Route")
            .log("🔁 Reversing Step 3")
            .setBody(exchangeProperty("inputPayload"))
            .setHeader(Exchange.HTTP_METHOD, constant(HttpMethod.POST))
            .setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
            .toD(step3ServiceUrl + "/step3/reverse");
    }
}