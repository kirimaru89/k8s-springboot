package com.example.orchestrationservice.route;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.http.base.HttpOperationFailedException;
import org.apache.camel.model.SagaCompletionMode;
import org.apache.camel.model.SagaPropagation;
import org.apache.camel.model.rest.RestBindingMode;
import org.springframework.stereotype.Component;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.Exchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class Step2Route extends RouteBuilder {
    private static final Logger log = LoggerFactory.getLogger(Step2Route.class);

    @Override
    public void configure() throws Exception {
        onException(HttpOperationFailedException.class)
            .handled(true) // Đánh dấu là đã xử lý lỗi, không để nó "nhảy ra ngoài"
            .process(exchange -> {
                HttpOperationFailedException exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, HttpOperationFailedException.class);
                String body = exception.getResponseBody();
                log.info("--------------- Step2Route: Error response from step2service: {}", body);

                // String originalRequestBody = exchange.getProperty("originalRequestBody", String.class);
                // exchange.getIn().setBody(originalRequestBody);
                // Xử lý ngoại lệ ở đây, ví dụ: ghi log hoặc trả về một phản hồi khác
                // HttpOperationFailedException exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, HttpOperationFailedException.class);
                // String body = exception.getResponseBody();
                // ApiResponseDto<Void> parsed = JsonUtils.readValue(body, new TypeReference<ApiResponseDto<Void>>() {});
                // parsed.setMessage("[TransactionService] " + parsed.getMessage());
                // exchange.getIn().setBody(parsed);
            })
            .to("direct:rollbackStep2");

        from("direct:step2")
            .saga()
            .process(exchange -> {
                // String input = exchange.getProperty("input", String.class);
                // String body = exchange.getIn().getBody(String.class);
                String body = exchange.getProperty("originalRequestBody", String.class);
                log.info("--------------- Before execute step2! Body: {}", body);
                // exchange.setProperty("originalRequestBody", body);

                exchange.getIn().setBody(body);
            })
            .log("Before execute step2!")
            .marshal().json()
            .to("http://step2service:8082/step2/execute")
            .process(exchange -> {
                String body = exchange.getIn().getBody(String.class);
                log.info("--------------- After execute step2! Body: {}", body);
            })
            .log("Step 2 completed successfully!")
            .end();
    }
}